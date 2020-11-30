package ro.kuberam.maven.plugins.expath;

import javax.xml.namespace.QName;

public interface PackageConstants {

    String EXPATH_PKG_NS = "http://exist-db.org/ns/expath-pkg";

    QName PACKAGE_ELEM_NAME = new QName(EXPATH_PKG_NS, "package");
    QName CONTENTS_ELEM_NAME = new QName(EXPATH_PKG_NS,"contents");
    QName PUBLIC_URI_ELEM_NAME = new QName(EXPATH_PKG_NS, "public-uri");
    QName RESOURCE_ELEM_NAME = new QName(EXPATH_PKG_NS, "resource");
    QName FILE_ELEM_NAME = new QName(EXPATH_PKG_NS, "file");
    QName XQUERY_ELEM_NAME = new QName(EXPATH_PKG_NS, "xquery");
    QName NAMESPACE_ELEM_NAME = new QName(EXPATH_PKG_NS, "namespace");

    String COMPONENTS_FILENAME = "components.xml";
}
