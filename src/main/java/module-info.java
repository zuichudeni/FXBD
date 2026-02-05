module FXBD {
    requires antlr4;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.antlr.antlr4.runtime;
    requires java.desktop;
    requires org.apache.commons.io;
    requires batik.transcoder;
    requires com.ibm.icu;
    requires java.naming;
    requires java.management;
    requires org.apache.commons.compress;
    requires com.dlsc.fxmlkit;
    requires javafx.fxml;
    requires java.sql;


    exports com.xx.UI.basic.progressBar;
    exports com.xx.UI.complex.textArea.view.dataFormat.example.java.lexer;
    exports com.xx.UI.complex.textArea.view.dataFormat.example.java.JavaImp;
    exports com.xx.antlr.XMLLexer.lexer;
    exports com.xx;
    exports com.xx.UI.util;
    exports com.xx.UI.complex.textArea.content;
    exports com.xx.UI.complex.textArea.content.segment;
    exports com.xx.UI.complex.textArea.view;
    exports com.xx.UI.ui;
    exports com.xx.UI.complex.textArea.view.dataFormat.example.java.parser;
    exports com.xx.UI.complex.textArea.view.dataFormat.symbolTable.scope;
    exports com.xx.UI.complex.textArea.view.dataFormat.symbolTable.symbol;
    exports com.xx.UI.complex.textArea.view.dataFormat.symbolTable.type;
    exports com.xx.UI.complex.search;

    opens com.xx to javafx.graphics;
    exports com.xx.UI.complex.textArea.content.listener;
    exports com.xx.UI.complex.textArea.view.dataFormat.example.java;
    exports com.xx.UI.complex.textArea.view.dataFormat.mark;
    exports com.xx.UI.complex.textArea.view.dataFormat.analyse;
    exports com.xx.UI.complex.textArea.view.dataFormat.example.regex;

    exports com.xx.UI.complex.BDTabPane;
    exports com.xx.demo;
}