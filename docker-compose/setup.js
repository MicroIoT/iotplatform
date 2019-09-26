db = db.getSiblingDB('iot');
db.createCollection('user');
db.user.insert(
	{"_class":"top.microiot.domain.User", 
		"username":"admin", 
		"password":"password", 
		"status":"enable", 
		"email":"13601161480@139.com", 
		"roles":["SYSTEM"]}
);