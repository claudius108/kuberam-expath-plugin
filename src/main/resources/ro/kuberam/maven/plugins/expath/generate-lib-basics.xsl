<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

	<xsl:output method="text" />

	<xsl:param name="javaPackageName" />
	<xsl:param name="libDirPath" />
	<xsl:param name="libVersion" />
	<xsl:param name="libUrl" />
	<xsl:param name="libArtifactId" />
	<xsl:param name="libName" />

	<xsl:variable name="javaPackageDirPath" select="concat($libDirPath, '/src/main/java/', translate($javaPackageName, '.', '/'), '/')" />
	<xsl:variable name="java-package-declaration" select="concat('package ', $javaPackageName, ';')" />

	<xsl:variable name="java-end-of-instruction-line">
		<xsl:text>";
</xsl:text>
	</xsl:variable>

	<xsl:template match="/">
		<xsl:variable name="module-namespace">
			<xsl:copy-of select="//element()[@id = 'module-namespace']" />
		</xsl:variable>
		<xsl:variable name="module-prefix">
			<xsl:copy-of select="//element()[@id = 'module-prefix']" />
		</xsl:variable>

<!-- 		<xsl:result-document href="{concat($libDirPath, '/pom.xml')}" method="xml" indent="yes"> -->
<!-- 			<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" -->
<!-- 				xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> -->

<!-- 				<modelVersion>4.0.0</modelVersion> -->

<!-- 				<parent> -->
<!-- 					<groupId>org.expath.libs</groupId> -->
<!-- 					<artifactId>base</artifactId> -->
<!-- 					<version>1.0-SNAPSHOT</version> -->
<!-- 				</parent> -->

<!-- 				<artifactId><xsl:value-of select="$libArtifactId" /></artifactId> -->
<!-- 				<version><xsl:value-of select="$libVersion" /></version> -->
<!-- 				<name><xsl:value-of select="$libName" /></name> -->
<!-- 				<url><xsl:value-of select="$libUrl" /></url> -->

<!-- 				<dependencies> -->
<!-- 					<xsl:copy-of select="document(concat('file://', $libDirPath, '/src/main/resources/org/expath/', $javaPackageName, '/dependencies.xml'))/element()/element()" /> -->
<!-- 				</dependencies> -->

<!-- 				<build> -->
<!-- 					<plugins> -->
<!-- 						<plugin> -->
<!-- 							<groupId>org.apache.maven.plugins</groupId> -->
<!-- 							<artifactId>maven-jar-plugin</artifactId> -->
<!-- 						</plugin> -->
<!-- 					</plugins> -->
<!-- 				</build> -->

<!-- 			</project> -->
<!-- 		</xsl:result-document> -->

		<xsl:result-document href="{concat($javaPackageDirPath, 'ErrorMessages.java')}" method="text">
			<xsl:value-of select="$java-package-declaration" />
			<xsl:text>
      
</xsl:text>
			<xsl:text>public class ErrorMessages {
</xsl:text>
			<xsl:for-each select="//element()[@id = 'summary-of-error-conditions']/*">
				<xsl:text>      public static String </xsl:text>
				<xsl:value-of select="replace(@key, ':', '_')" />
				<xsl:text> = "</xsl:text>
				<xsl:value-of select="concat(@key, ': ', .)" />
				<xsl:text>";
</xsl:text>
			</xsl:for-each>
			<xsl:text>}</xsl:text>
		</xsl:result-document>

		<xsl:result-document href="{concat($javaPackageDirPath, 'ModuleDescription.java')}" method="text">
			<xsl:value-of select="$java-package-declaration" />
			<xsl:text>

</xsl:text>
			<xsl:text>
/**
 * Module description.
 * 
 * @author Claudius Teodorescu &lt;claudius.teodorescu@gmail.com&gt;
 */      
</xsl:text>
			<xsl:text>public class ModuleDescription {
</xsl:text>
			<xsl:text>      public final static String VERSION = "</xsl:text>
			<xsl:value-of select="concat($libVersion, $java-end-of-instruction-line)" />
			<xsl:text>      public final static String NAMESPACE_URI = "</xsl:text>
			<xsl:value-of select="concat($module-namespace, $java-end-of-instruction-line)" />
			<xsl:text>      public final static String PREFIX = "</xsl:text>
			<xsl:value-of select="concat($module-prefix, $java-end-of-instruction-line)" />
			<xsl:text>      public final static String MODULE_NAME = "</xsl:text>
			<xsl:value-of select="concat($libName, $java-end-of-instruction-line)" />
			<xsl:text>      public final static String MODULE_DESCRIPTION = "</xsl:text>
			<xsl:value-of select="concat('A ', //element()[@id = 'module-description'], $java-end-of-instruction-line)" />
			<xsl:text>}
</xsl:text>
		</xsl:result-document>

	</xsl:template>
</xsl:stylesheet>