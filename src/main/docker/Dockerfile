#FROM registry.eu-gb.bluemix.net/ibmliberty
FROM registry.ng.bluemix.net/ibmliberty
COPY server.xml /opt/ibm/wlp/usr/servers/defaultServer/
ADD bluebox.war /opt/ibm/wlp/usr/servers/defaultServer/apps/
ENV LICENSE accept