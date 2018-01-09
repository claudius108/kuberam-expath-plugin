xquery version "3.1";

declare namespace pkg = "http://expath.org/ns/pkg";

<package xmlns="http://exist-db.org/ns/expath-pkg" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	{
		for $component in //pkg:components/*
		
		return
			<java>
				<namespace>{$component/pkg:public-uri/text()}</namespace>
				<class>{$component/pkg:file/text()}</class>
			</java>		
	}
</package>