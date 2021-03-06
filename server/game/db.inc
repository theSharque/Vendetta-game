<?php

ini_set( 'display_errors', 0 );
ini_set( 'log_errors', 1 );
ini_set( 'error_log', 'errlog.log' );

function debug_it( $string ) {
  $time = date("H:i:s");
  $prn = print_r( $string, true );
  file_put_contents( "/var/www/vendetta/game/debug.log", "$time\t$prn\r\n", FILE_APPEND );
}

function db_init() {
  static $dblink;

  if( $dblink === null ) {
    $dblink = mysql_connect( 'localhost', 'vendetta', 'password' ) or die("Could not connect: " . mysql_error());
    mysql_select_db( 'vendetta' ) or die ('Can\'t use db : ' . mysql_error());
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

function auth() {
  global $user;

  if( isset( $_GET['b'] ) && $_GET['b'] ) {
    $imei = $_GET['b'];
    db_safe( $imei );

    $user = db_fetch_row( "SELECT u.id, u.login, u.die, u.imei, u.bsid, u.phone, u.status, u.game_id, u.photo_id, u.reg_date, u.location,
                                  u.games, u.wins, u.kills, p.file photo_url, p.status approve, g.admin_id game_admin, g.status game_status,
                                  g.location game_loc, v.pass vpass, v.ans vans
                             FROM users u 
                        LEFT JOIN games g ON g.id = u.game_id
                        LEFT JOIN photos p ON p.id = u.photo_id
                        LEFT JOIN victims v ON v.victim_id = u.id
                            WHERE u.imei = '$imei'" );

    if( $user['id'] ) {
      $messages = db_fetch_val( "SELECT count(*) cnt
                                   FROM message m
                                  WHERE m.type != 6 AND m.user_id = {$user['id']} AND m.status = 2", 'cnt' );

      db_query( "UPDATE users SET ip = '{$_SERVER['REMOTE_ADDR']}' WHERE id = {$user['id']}" );
    } else {
      $messages = 0;
    }

    $user['location'] = explode( ':', $user['location'] );
    $user['messages'] = $messages;

    if( isset( $user['imei'] ) && $user['imei'] == $imei ) {
      return true;
    } else {
      return false;
    }
  } else {
    return false;
  }
}

function printOut( $text ) {
  global $user;

  echo mb_convert_encoding( $text, 'UTF-8', 'Windows-1251' );
}

function sendMessage( $to, $from, $type, $arg1, $arg2 ) {
  db_query( "INSERT INTO message (user_id, sender_id, type, arg1, arg2) VALUES ( $to, $from, $type, $arg1, '$arg2' )" );
  $imei = db_fetch_val( "SELECT imei FROM users WHERE id = $to", 'imei' );

  if( $imei ) {
    $imei = str_pad( $imei, 16, '0000000000000000', STR_PAD_LEFT );
    db_query( "INSERT IGNORE queue ( imei, msg ) VALUES ( '$imei', 1 )" );
  }
}

function distance( $lat1, $lon1, $lat2, $lon2 ) {
    $distance = 100000;

    if( is_numeric( $lat1 ) && is_numeric( $lon1 ) && is_numeric( $lat2 ) && is_numeric( $lon2 ) ) {
      $radius = 6371000;
  
      $delta_Rad_Lat = deg2rad($lat2 - $lat1);
      $delta_Rad_Lon = deg2rad($lon2 - $lon1);
      $rad_Lat1 = deg2rad($lat1);
      $rad_Lat2 = deg2rad($lat2);
  
      $sq_Half_Chord = sin($delta_Rad_Lat / 2) * sin($delta_Rad_Lat / 2) + cos($rad_Lat1) * cos($rad_Lat2) * sin($delta_Rad_Lon / 2) * sin($delta_Rad_Lon / 2);
      $ang_Dist_Rad = 2 * asin(sqrt($sq_Half_Chord));
      $distance = $radius * $ang_Dist_Rad;  
    }

    return round( $distance );
}

function killPlayer( $id, $type = 3 ) {

  global $user;

  $game = db_fetch_val( "SELECT game_id FROM users WHERE id = $id", 'game_id' );
  if( $game == 0 ) {
    return;
  }

  $users = db_fetch_array( "SELECT id, login FROM users WHERE die = 0 AND game_id = $game" );

  if( count( $users ) > 2 ) {
    foreach( $users as $item ) {
      if( $type == 3 || $item['id'] != $id ) {
        sendMessage( $item['id'], $id, 9, $game, $id );
      }
    }

    $vid = db_fetch_val( "SELECT victim_id id FROM victims WHERE hunter_id = $id", 'id' );

    db_query( "DELETE FROM victims WHERE hunter_id = $id" );
    db_query( "UPDATE victims SET victim_id = $vid WHERE victim_id = $id" );

    if( $type == 4 ) {
      db_query( "UPDATE users SET games = games + 1, die = 1 WHERE id = $id" );
      db_query( "UPDATE users SET kills = kills + 1 WHERE id = {$user['id']}" );

      db_query( "UPDATE ingame SET kills = kills + 1 WHERE user_id = {$user['id']} AND game_id = $game" );
    } else {
      db_query( "UPDATE users SET die = 1 WHERE id = $id" );
    }

    db_query( "UPDATE ingame SET status = 0, killer_id = {$user['id']} WHERE user_id = $id AND game_id = $game" );
  } else {
    $etype = db_fetch_val( "SELECT end_type FROM games WHERE id = $game" );

    foreach( $users as $item ) {
      if( $item['id'] != $id ) {
        if( $etype == 0 ) {
          sendMessage( $item['id'], 0, 7, 5, "" );
          db_query( "UPDATE users SET kills = kills + 1, wins = wins + 1 WHERE id = {$item['id']}" );

          $other = db_fetch_array( "SELECT id FROM users WHERE id != {$item['id']} AND game_id = $game" );
          foreach( $other as $looser ) {
            sendMessage( $looser['id'], 0, 7, 8, $item['login'] );
          }
        } else {
          $max = db_fetch_val( "SELECT MAX( kills ) mx FROM ingame WHERE game_id = $game", 'mx' );
          $winer = db_fetch_array( "SELECT user_id FROM ingame WHERE kills = $max" );
          if( count( $winer ) > 1 ) {

            $other = db_fetch_array( "SELECT id FROM users WHERE id != {$item['id']} AND game_id = $game" );
            foreach( $other as $looser ) {
              sendMessage( $looser['id'], 0, 7, 6 );
            }
          } else {
            $winer = array_shift( $winer );
            sendMessage( $winer['user_id'], 0, 7, 5, "" );
            db_query( "UPDATE users SET kills = kills + 1, wins = wins + 1 WHERE id = {$item['id']}" );

            $other = db_fetch_array( "SELECT id FROM users WHERE id != {$item['id']} AND game_id = $game" );
            foreach( $other as $looser ) {
              sendMessage( $looser['id'], 0, 7, 8, $item['login'] );
            }
          }
        }

        db_query( "UPDATE games SET status = 3 WHERE id = $game" );
      } else {
        if( $type == 3 ) {
          sendMessage( $item['id'], $id, 9, $game, $id );
        }
      }

      db_query( "DELETE FROM victims WHERE hunter_id = {$item['id']}" );

      if( $type == 4 ) {
        db_query( "UPDATE users SET games = games + 1, die = 1 WHERE id = $id" );
      } else {
        db_query( "UPDATE users SET die = 1 WHERE id = $id" );
      }
    }

    if( $type == 4 ) {
      db_query( "UPDATE users SET kills = kills + 1 WHERE id = {$user['id']}" );
      db_query( "UPDATE ingame SET kills = kills + 1 WHERE user_id = {$user['id']} AND game_id = $game" );
    }
    if( $type == 4 ) {
      db_query( "UPDATE ingame SET status = 0, killer_id = {$user['id']} WHERE user_id = $id AND game_id = $game" );
    } else {
      db_query( "UPDATE ingame SET status = 0, killer_id = 0 WHERE user_id = $id AND game_id = $game" );
    }

    db_query( "DELETE FROM victims WHERE game_id = $game" );
  }

  if( $type == 4 ) {
    sendMessage( $id, 0, 8, 0, "" );
  }
}

function startGame( $id ) {
  $game = db_fetch_row( "SELECT g.id, g.start_type, g.start_count, u.location, count( u2.id ) in_game, g.status, g.victims_count
                           FROM games g
                     INNER JOIN users u ON u.id = g.admin_id
                     INNER JOIN users u2 ON u2.game_id = g.id
                          WHERE g.id = $id
                       GROUP BY g.id" );

  if( $game['status'] == 1 && ( ( $game['start_type'] == 0 && $game['start_count'] == $game['in_game'] ) || ( $game['start_type'] == 1 ) ) ) {
    db_query( "UPDATE games SET location = '{$game['location']}', status = 2 WHERE id = {$game['id']}" );
    db_query( "UPDATE users SET ping_date = now() WHERE game_id = {$game['id']}" );

    $players = db_fetch_array( "SELECT id FROM users WHERE game_id = {$game['id']}" );

    if( count( $players ) < $game['victims_count'] ) {
      $game['victims_count'] = count( $players ) - 1;
      db_query( "UPDATE games SET victims_count = {$game['victims_count']} WHERE id = $id" );
    }

    $hunters = array();
    foreach( $players as $item ) {
      db_query( "INSERT INTO ingame ( game_id, user_id ) VALUES ( {$game['id']}, {$item['id']} )" );
      $hunters[] = $item['id'];
      sendMessage( $item['id'], 0, 6, 0, "" );
      sendMessage( $item['id'], 0, 7, 2, "" );
    }

    shuffle( $hunters );
    $victims = $hunters;
    array_push( $victims, array_shift( $victims ) );

    for( $i = 0; $i < count( $hunters ); $i++ ) {
      for( $t = 0; $t < $game['victims_count']; $t++ ) {
        if( $i + $t < count( $victims ) ) {
          db_query( "INSERT INTO victims ( game_id, hunter_id, victim_id, pass, ans ) VALUES ( {$game['id']}, {$hunters[$i]}, {$victims[$i + $t]}, md5(rand()), md5(rand()) )" );
        } else {
          $c = count( $victims ) - $i + $t;
          db_query( "INSERT INTO victims ( game_id, hunter_id, victim_id, pass, ans ) VALUES ( {$game['id']}, {$hunters[$i]}, {$victims[$c + $t]}, md5(rand()), md5(rand()) )" );
        }
      }
    }

    return true;
  } else {
    return false;
  }
}
