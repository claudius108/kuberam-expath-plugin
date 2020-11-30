xquery version "3.1";

declare namespace map = "http://www.w3.org/2005/xpath-functions/map";
declare namespace html = "http://www.w3.org/1999/xhtml";

declare variable $spec-file-paths external;

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>EXPath Specifications Index</title>
	</head>
	<body>
		<h2>EXPath Specifications Index</h2>
			{
				for $spec-file-path in tokenize($spec-file-paths, ",")
				let $spec-id := replace($spec-file-path, "^(.*/)(.*?)\.\w+$", "$2")
				let $spec-path := concat($spec-id, '/', $spec-id, '.html')
				let $spec-file := doc("file://" || $spec-file-path)/html:html/html:body
				
				return
					(
						<h4>
							<a href="{$spec-path}">{$spec-file/html:div[@class = 'head']/html:h1/text()}</a>
						</h4>
						,
						<h5>{$spec-file/html:div[2]/html:p}</h5>
					)
			}
	</body>
</html>