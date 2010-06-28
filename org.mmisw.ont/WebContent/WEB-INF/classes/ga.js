var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
try{
  var pageTracker = _gat._getTracker("${ga.uanumber}");
  pageTracker._setDomainName("${ga.domainName}");
  pageTracker._trackPageview("${ga.pageName}");
} catch(err) {
  console.log('Analytics: Error while executing pageTracker stuff', err);
}