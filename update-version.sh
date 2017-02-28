#!/bin/sh

ver=$1
if [ -z "$ver" ]
then 
  echo You must provide the product version \(e.g. \"update-version 1.2.4\"\)
exit -1
fi


function updateVersion() {
    pom_file=$1
    sed -i "s|\(<version>\)[^<>]*\(-SNAPSHOT</version>\)|\1${ver}\2|" ${pom_file}
}

sed -i "s|\(version=\"\)[^<>]*\(.qualifier\"\)|\1${ver}\2|" repository/org.csstudio.fzj.cosy.css.product
updateVersion "pom.xml"
updateVersion "features/pom.xml"
updateVersion "plugins/pom.xml"
updateVersion "repository/pom.xml"

java -jar ImageLabeler-2.0.jar ${ver} 462 53 plugins/org.csstudio.fzj.cosy.css.product/splash-template.bmp plugins/org.csstudio.fzj.cosy.css.product/splash.bmp BLACK
