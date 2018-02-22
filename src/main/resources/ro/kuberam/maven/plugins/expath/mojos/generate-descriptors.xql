xquery version "3.1";

declare namespace pkg = "http://expath.org/ns/pkg";

declare variable $components := //pkg:components/*;
declare variable $java-class-nid := "urn:java:class:";

<package xmlns="http://exist-db.org/ns/expath-pkg" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	{
		(: process class declarations for exist.xml :)
		for $component in $components
		let $urn := $component/pkg:file
		
		return (
			if (starts-with($urn, $java-class-nid))
			then
				let $java-class-name := substring-after($urn, $java-class-nid)
				
				return
					<java>
						<namespace>{$component/pkg:public-uri/text()}</namespace>
						<class>{$java-class-name}</class>
					</java>
			else ()
		)		
	}
</package>