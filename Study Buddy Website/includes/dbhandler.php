<?php
class DBConnection {
	protected $conn;
	protected $connerror;
	protected $username = "studybuddy";
	protected	$password = "tQ1F2LXHBT4EUgQk";
	protected	$host = "localhost";
	protected	$dbname = "studybuddy";
	function __construct() {
		$this->GetDBConnection();
	}
	function __destruct() {
		$this->conn = null;
	}
	function GetDBConnection() {
		try {
			$this->conn = new PDO("mysql:host=" . $this->host . ";dbname=" . $this->dbname, $this->username, $this->password);
			$this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		}
		catch(PDOException $e)
  	{
  	  $this->connerror = $e->getMessage();
  	}
	}
	function GetConnError() {
		return $this->connerror;
	}
}

class SecureSessionHandler extends SessionHandler {
	protected $key, $name, $cookie;
	public function __construct($key = '`ld,c.ro593=+-pls;e.rtu8', $name = "StudyBuddySession", $cookie = []) {
		$this->key = $key;
		$this->name = $name;
		$this->cookie = $cookie;
		$this->cookie += [
            'lifetime' => 0,
            'path'     => ini_get('session.cookie_path'),
            'domain'   => ini_get('session.cookie_domain'),
            'secure'   => isset($_SERVER['HTTPS']),
            'httponly' => true
        ];
		$this->setup();
	}
	protected function setup() {
    ini_set('session.use_cookies', 1);
    ini_set('session.use_only_cookies', 1);
		ini_set('session.save_handler', 'files');
		ini_set('session.cookie_domain', 'localhost');
		ini_set('session.entropy_file', '/dev/urandom');
		ini_set('session.entropy_length', 32);
		ini_set('session.hash_function', 'sha256');
		ini_set('session.hash_bits_per_character', 5);
    session_name($this->name);
    session_set_cookie_params(
      $this->cookie['lifetime'], $this->cookie['path'],
      $this->cookie['domain'], $this->cookie['secure'],
      $this->cookie['httponly']
    );
  }
	public function start() {
    if (session_id() === '') {
      if (session_start()) {
        //return (mt_rand(0, 4) === 0) ? $this->refresh() : true; // 1/5
				return true;
      }
    }
    return false;
  }
	public function forget() {
    if (session_id() === '') {
      return false;
    }
    $_SESSION = [];
    setcookie(
        $this->name, '', time() - 42000,
        $this->cookie['path'], $this->cookie['domain'],
        $this->cookie['secure'], $this->cookie['httponly']
    );
    return session_destroy();
	}
	public function refresh() {
    return session_regenerate_id(true);
	}
	public function read($id) {
    return mcrypt_decrypt(MCRYPT_3DES, $this->key, parent::read($id), MCRYPT_MODE_ECB);
	}

	public function write($id, $data) {
    return parent::write($id, mcrypt_encrypt(MCRYPT_3DES, $this->key, $data, MCRYPT_MODE_ECB));
	}

	public function isExpired($ttl = 30) {
    $last = isset($_SESSION['_last_activity']) ? $_SESSION['_last_activity'] : false;
    if ($last !== false && time() - $last > $ttl * 60) {
      return true;
    }
    $_SESSION['_last_activity'] = time();
      return false;
  }
  public function isValid() {
    return ! $this->isExpired();
  }
  public function get($name) {
    $parsed = explode('.', $name);
    $result = $_SESSION;
    while ($parsed) {
      $next = array_shift($parsed);
      if (isset($result[$next])) {
        $result = $result[$next];
      }
			else return null;
    }
    return $result;
  }
  public function put($name, $value) {
		$parsed = explode('.', $name);
    $session =& $_SESSION;
    while (count($parsed) > 1) {
      $next = array_shift($parsed);
      if ( ! isset($session[$next]) || ! is_array($session[$next])) {
        $session[$next] = [];
      }
      $session =& $session[$next];
    }
    $session[array_shift($parsed)] = $value;
  }
}
?>
