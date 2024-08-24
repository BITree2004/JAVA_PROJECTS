package info.kgeorgiy.ja.dmitriev.i18n.report;

/*package-private*/ class Templates {
    /*package-private*/ static final String HEADER = """
            <!DOCTYPE html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
            </head>
            """;
    /*package-private*/ static final String REPORT = """
            <!DOCTYPE html>
            <html>
            %s
            <body>
            %s
            %s
            %s
            %s
            %s
            %s
            %s
            </body>
            </html>
            """;
    /*package-private*/ static final String SUMMARY = """
            <h4>
                %s
            </h4>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            """;
    /*package-private*/ static final String TITLE = "<h2>%s</h2>";
    /*package-private*/ static final String SECTION = """
            <h4>
                %s
            </h4>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            <p>%s</p>
            """;
}
