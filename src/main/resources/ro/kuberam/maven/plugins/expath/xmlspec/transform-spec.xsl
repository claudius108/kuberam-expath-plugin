<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:h="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" version="2.0">

	<xsl:import href="xmlspec.xsl" />

	<xsl:output method="xhtml" version="1.0" omit-xml-declaration="yes" encoding="utf-8" indent="yes" />

	<xsl:param name="googleAnalyticsAccountId" />

	<xsl:template match="/">
		<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<xsl:next-match />
	</xsl:template>

	<xsl:template match="h:html/h:body" mode="postproc">
		<xsl:copy>
			<xsl:copy-of select="@*" />
			<xsl:apply-templates select="node()" mode="postproc" />
			<xsl:if test="$googleAnalyticsAccountId != ''">
				<script type="text/javascript">
					var _gaq = _gaq || [];
					<xsl:text>_gaq.push(['_setAccount', '</xsl:text>
					<xsl:value-of select="normalize-space($googleAnalyticsAccountId)" />
					<xsl:text>']);</xsl:text>
					_gaq.push(['_trackPageview']);
					(function() {
					var ga = document.createElement('script'); ga.type =
					'text/javascript';
					ga.async = true;
					ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www')
					+
					'.google-analytics.com/ga.js';
					var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga,
					s);
					})();
				</script>
			</xsl:if>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
