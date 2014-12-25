1. check java runtime settings before running startup.sh
	(1) 'JAVA_HOME' in setclasspath.sh
	(2) 'JAVA_OPTS' in startup.sh

2. check settings in resource/data-origin-generator.properties
	(1) '<resource-ref>' in template/webapp/WEB-INF/web.xml
	(2) '<onEditingDBResourceName>', '<productionDBResourceName>' in template/webapp/WEB-INF/data-origin/DataOriginSetting.xml

2. Run the startup.sh
	(1) generate meta. (Should be generated 1st generating). Command: 
		./startup.sh 
	
	(2) generate web. overwrite all files. Command:
		./startup.sh web wa
		
	(3) generate web. overwrite files which generated source only. Command:
		./startup.sh web wg
