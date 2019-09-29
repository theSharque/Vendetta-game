<?php

ini_set( 'display_errors', 0 );
ini_set( 'log_errors', 1 );
ini_set( 'error_log', 'errlog.log' );

function debug_it( $string ) {
  $time = date("H:i:s");
  $prn = print_r( $string, true );
  file_put_contents( "/var/www/vendetta/vcm/debug.log", "$time\t$prn\r\n", FILE_APPEND );
}

function db_init() {
  static $dblink;

  if( $dblink === null ) {
    $dblink = mysql_connect( 'localhost', 'vendetta', 'XKAqdLevEWsPDzMd' ) or die("Could not connect: " . mysql_error());
    mysql_select_db( 'vendetta' ) or die ('Can\'t use db : ' . mysql_error());
//    mysql_query( "SET character_set_client = UTF-8, character_set_connection = UTF-8, character_set_results = UTF-8" );
    mysql_query( "SET character_set_client = cp1251, character_set_connection = cp1251, character_set_results = cp1251" );
  }

  return $dblink;
}

function db_query( $sql ) {
  if( db_init() ) {

    $result = mysql_query( $sql );

    if( !$result ) {
      if( mysql_errno() == 1062 ) {
        return $result;
      } elseif( mysql_errno() == 1213 || mysql_errno() == 1205 || mysql_errno() == 1317 ) {
        debug_it( 'Error '.mysql_errno().' we need to try again...' );
        debug_it( $sql );
        sleep( 4 );
        $result = db_query( $sql );
      } else {
        debug_it( mysql_errno().' : '.mysql_error() );
        $back = debug_backtrace();
        debug_it( $back );
      }
    }

    return $result;
  }
}

function db_fetch_array( $sql, $in_cache = false, $order = null ) {
  static $l_cache;

  if( $sql == 'clear' ) {
    $l_cache = array();
    return;
  }

  if( $in_cache ) {
    if( is_array( $l_cache ) && array_key_exists( $sql, $l_cache ) ) {
      return $l_cache[$sql];
    }
  }

  $out = array();

  if( $result = db_query( $sql ) ) {
    if( $order === null ) {
      while ( $row = mysql_fetch_assoc( $result ) ) {
        $out[] = $row;
      }
    } else {
      while ( $row = mysql_fetch_assoc( $result ) ) {
        $out[$row[$order]] = $row;
      }
    }
  }

  if( $in_cache ) {
    $l_cache[$sql] = $out;
  }

  return $out;
}

function db_fetch_row( $sql, $in_cache = false ) {
  static $l_cache;

  if( $sql == 'clear' ) {
    $l_cache = array();
    return;
  }

  if( $in_cache ) {
    if( is_array( $l_cache ) && array_key_exists( $sql, $l_cache ) ) {
      return $l_cache[$sql];
    }
  }

  $out = array();

  if( $result = db_query( $sql ) ) {
    $out = mysql_fetch_assoc( $result );
  }

  if( $in_cache ) {
    $l_cache[$sql] = $out;
  }

  return $out;
}

function db_fetch_val( $sql, $val = 'id', $in_cache = false ) {
  static $l_cache;

  if( $sql == 'clear' ) {
    $l_cache = array();
    return;
  }

  if( $in_cache ) {
    if( is_array( $l_cache ) && array_key_exists( $sql, $l_cache ) ) {
      return $l_cache[$sql];
    }
  }

  $out = null;

  if( $result = db_query( $sql ) ) {
    if( $result ) {
      $arr = mysql_fetch_assoc( $result );
      if( isset( $arr[ $val ] ) ) {
        $out = $arr[ $val ];
      }
    }
  }

  if( $in_cache ) {
    $l_cache[$sql] = $out;
  }

  return $out;
}

function db_safe( &$var ) {
  if( $var !== null ) {
    $var = mysql_escape_string( $var );
  }

  return $var;
}
