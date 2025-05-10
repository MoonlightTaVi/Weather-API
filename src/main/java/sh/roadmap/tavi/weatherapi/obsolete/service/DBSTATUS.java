package sh.roadmap.tavi.weatherapi.obsolete.service;

/**
 * Database connection status <br>
 * NOT_INITIALIZED - hasn't tried to connect <br>
 * FAILED_TO_CONNECT - DB not running, or credentials are wrong <br>
 * CONNECTED - everything is OK <br>
 * FAILED_TO_UPDATE - could not put a value to/get a value from the DB (but every other thing is fine) <br>
 * IDLE - utilizing a .json file caching instead of Redis
 */
public enum DBSTATUS {
	NOT_INITIALIZED,
	FAILED_TO_CONNECT,
	CONNECTED,
	FAILED_TO_UPDATE,
	IDLE
}