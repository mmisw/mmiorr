try {
	__trim__("foo");
}
catch(err) {
	console.log('Analytics: __trim__ seems undefined', err);
	console.log('Analytics: Defining __trim__');
	function __trim__( str ){ return (str || "").replace( /^\s+|\s+$/g, "" ); }
}

try {
  var pageTracker = _gat._getTracker("${ga.uanumber}");
  pageTracker._setDomainName("${ga.domainName}");
  pageTracker._trackPageview("${ga.pageName}");
} 
catch(err) {
  console.log('Analytics: Error while executing pageTracker stuff', err);
}