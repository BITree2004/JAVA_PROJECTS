package info.kgeorgiy.ja.dmitriev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Is an instance of {@link Crawler}.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
@SuppressWarnings("unused")
public class WebCrawler implements AdvancedCrawler {
    private final static String USAGE = "WebCrawler url [depth [downloads [extractors [perHost]]]]";
    private final static int UNLIMITED = Integer.MAX_VALUE;
    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService executorService;
    private final int perHost;
    private final Map<String, DownloaderQueue> hostRestrictions = new ConcurrentHashMap<>();

    /**
     * Creates an instance with arguments to work with.
     *
     * @param downloader  allows you to download pages and extract links from them
     * @param downloaders maximum number of simultaneously loaded pages
     * @param extractors  maximum number of pages from which links are retrieved simultaneously
     * @param perHost     the maximum number of pages downloaded simultaneously from one host. To determine the host, you should use the getHost method of the URLUtils class from the tests
     */
    public WebCrawler(
            final Downloader downloader,
            final int downloaders,
            final int extractors,
            final int perHost
    ) {
        checkIntegerNumber(downloaders, "Downloaders");
        checkIntegerNumber(extractors, "Extractors");
        checkIntegerNumber(perHost, "PerHost");
        this.downloader = downloader;
        this.downloadService = Executors.newFixedThreadPool(downloaders);
        this.executorService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * Provides a console interface over {@link WebCrawler#download(String, int)}.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        if (args == null
                || args.length < 1
                || 5 < args.length
                || Arrays.stream(args).anyMatch(Objects::isNull)
        ) {
            System.err.println(USAGE);
            return;
        }

        final var link = args[0];
        final var depth = getOrDefault(args, 1, "Depth");
        final var downloads = getOrDefault(args, 2, "Downloads");
        final var extractors = getOrDefault(args, 3, "Extractors");
        final var perHost = getOrDefault(args, 4, "PerHost");
        try (final var crawler = new WebCrawler(
                new CachingDownloader(0),
                downloads,
                extractors,
                perHost
        )) {
            final var res = crawler.download(link, depth);
            System.out.println("Downloaded:");
            res.getDownloaded().forEach(System.out::println);
            final var errors = res.getErrors();
            if (!errors.isEmpty()) {
                System.out.println("ERRORS:");
                errors.forEach((url, e) -> System.out.println(url + " Reason: " + e.getMessage()));
            }
        } catch (final IOException e) {
            System.err.println("Error creating CachingDownloader! Reason:" + e.getMessage());
        }
    }

    private static void checkCollection(final Collection<String> collection) {
        Objects.requireNonNull(collection);
        collection.forEach(Objects::requireNonNull);
    }

    @Override
    public Result download(final String url, final int depth, final Set<String> excludes) {
        checkCollection(excludes);
        return new CrawlerTask(null, new ArrayList<>(excludes)).getHost(url, depth);
    }

    @Override
    public Result advancedDownload(final String url, final int depth, final List<String> hosts) {
        checkCollection(hosts);
        final var set = new HashSet<String>();
        set.addAll(hosts);
        return new CrawlerTask(set, List.of()).getHost(url, depth);
    }

    @Override
    public void close() {
        close(downloadService);
        close(executorService);
    }

    private static void close(final ExecutorService executorService) {
        executorService.shutdown();
        boolean terminated = false;
        while (!terminated) {
            try {
                terminated = executorService.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ignored) {
            }
        }
    }

    private static void checkIntegerNumber(final int arg, final String name) {
        if (arg <= 0) {
            throw new IllegalArgumentException(name + " must be positive integer!");
        }
    }

    private static int getOrDefault(final String[] args, final int index, final String name) {
        if (index < args.length) {
            try {
                return Integer.parseInt(args[index]);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(name + " must be integer!", e);
            }
        } else {
            return UNLIMITED;
        }
    }

    private class DownloaderQueue {
        private final AtomicInteger cnt = new AtomicInteger(0);
        private final AtomicInteger wait = new AtomicInteger(0);
        private final String host;
        private final Queue<Runnable> runnables = new ArrayDeque<>();

        private DownloaderQueue(final String host) {
            this.host = host;
        }

        private synchronized void run() {
            if (cnt.get() < perHost) {
                final var runnable = runnables.poll();
                if (runnable != null) {
                    cnt.incrementAndGet();
                    downloadService.submit(() -> {
                        runnable.run();
                        cnt.decrementAndGet();
                        run();
                    });
                }
            }
        }

        private synchronized void add(final Runnable runnable) {
            runnables.add(runnable);
            run();
        }

        private void delete() {
            hostRestrictions.compute(host, (k, v) -> {
                if (cnt.get() + wait.get() == 0) {
                    v = null;
                }
                return v;
            });
        }
    }

    private class CrawlerTask {
        private final Set<String> successfulLinks = ConcurrentHashMap.newKeySet();
        private final Set<String> notProcessed = ConcurrentHashMap.newKeySet();
        private final Map<String, IOException> failedLinks = new ConcurrentHashMap<>();
        private final Set<String> visitedHost = ConcurrentHashMap.newKeySet();
        private final Set<String> hosts;
        private final List<String> excludes;
        private final ConcurrentLinkedQueue<String> layer = new ConcurrentLinkedQueue<>();
        private Phaser layerPhaser;

        private CrawlerTask(final Set<String> hosts, final List<String> excludes) {
            this.hosts = hosts;
            this.excludes = excludes;
        }

        private void addFailed(final String link, final IOException e) {
            failedLinks.put(link, e);
        }

        private boolean isExcludes(final String link) {
            return excludes.stream().anyMatch(link::contains);
        }

        private void download(final String link, final String host) {
            boolean isFailed = true;
            try {
                final var doc = downloader.download(link);
                successfulLinks.add(link);
                executorService.submit(() -> extractLinks(doc, link));
                isFailed = false;
            } catch (final IOException e) {
                addFailed(link, e);
            } finally {
                if (isFailed) {
                    layerPhaser.arrive();
                }
            }
        }

        private DownloaderQueue getDownloadQueue(final String host) {
            return hostRestrictions.computeIfAbsent(host, (x) -> new DownloaderQueue(host));
        }

        private void getHost(final String link) {
            boolean isFailed = true;
            try {
                if (isExcludes(link)) {
                    notProcessed.add(link);
                    return;
                }
                final var host = URLUtils.getHost(link);
                if (hosts != null && !hosts.contains(host)) {
                    notProcessed.add(link);
                    return;
                }
                visitedHost.add(host);
                final Runnable runnable = () -> download(link, host);
                final var downloaderQueue = hostRestrictions.compute(host, (k, v) -> {
                    if (v == null) {
                        v = new DownloaderQueue(host);
                    }
                    v.wait.incrementAndGet();
                    return v;
                });
                downloaderQueue.add(runnable);
                downloaderQueue.wait.decrementAndGet();
                isFailed = false;
            } catch (final IOException e) {
                addFailed(link, e);
            } finally {
                if (isFailed) {
                    layerPhaser.arrive();
                }
            }
        }

        private void extractLinks(final Document doc, final String link) {
            try {
                layer.addAll(doc.extractLinks());
            } catch (final IOException e) {
                addFailed(link, e);
            } finally {
                layerPhaser.arrive();
            }
        }

        private Set<String> downloadLayer(final Set<String> linksToVisit) {
            layerPhaser = new Phaser(linksToVisit.size() + 1);
            layer.clear();
            linksToVisit
                    .forEach(link -> downloadService.submit(() -> getHost(link)));
            // start execution
            layerPhaser.arriveAndAwaitAdvance();
            return layer.stream()
                    .filter(x -> !successfulLinks.contains(x)
                            && !failedLinks.containsKey(x)
                            && !notProcessed.contains(x))
                    .collect(Collectors.toCollection(HashSet::new));
        }

        private Result getHost(final String link, final int depth) {
            checkIntegerNumber(depth, "Depth");
            var actualSet = Set.of(link);
            for (int i = 0; i < depth; i++) {
                actualSet = downloadLayer(actualSet);
            }
            visitedHost.forEach(x -> hostRestrictions.get(x).delete());
            return new Result(List.copyOf(successfulLinks), failedLinks);
        }
    }
}
