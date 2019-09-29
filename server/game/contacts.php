<?php

  include( 'db.inc' );
  global $user;

  if( auth() && $_POST['data'] && isset( $_GET['g'] ) && is_numeric( $_GET['g'] ) ) {
    $temp = explode( ',', $_POST['data'] );
    $contacts = array();
    $tels = array();

    $games = $_GET['g'];
    db_safe( $games );

    array_pop( $temp );
    foreach( $temp as $key => $item ) {
      $vals = explode( ':', $item );
      $contacts[$vals[0]] = explode( ';', $vals[1] );
      array_pop( $contacts[$vals[0]] );
      foreach( $contacts[$vals[0]] as $val ) {
        $tels[substr( $val, -9 )] = $vals[0];
      }
    }

    $in = array_keys( $tels );

    $in = "'".implode( "','", $in )."'";
    if( $games == 0 ) {
      $found = db_fetch_array( "SELECT u.id, u.phone, u.login, p.file, u.wins, u.kills, u.location, u.games
                                  FROM users u
                             LEFT JOIN photos p ON p.id = u.photo_id
                                 WHERE u.id != {$user['id']} AND u.phone IN ( $in )" );
    } else {
      $found = db_fetch_array( "SELECT u.id, u.phone, u.login, p.file, u.wins, u.kills, u.location
                                  FROM users u
                            INNER JOIN games g ON g.id = u.game_id
                             LEFT JOIN photos p ON p.id = u.photo_id
                                 WHERE u.id != {$user['id']} AND g.game_type = 1 AND g.id = $games AND u.phone IN ( $in )" );
    }

		$is_open = db_fetch_val( "SELECT game_type FROM games WHERE id = {$user['game_id']}", 'game_type' );

    printOut( "cnt=".count( $found ) );

    $l = 0;
    foreach( $found as $item ) {
     	printOut( "&id$l={$tels[$item['phone']]}&u$l={$item['id']}&n$l={$item['login']}&p$l={$item['file']}&g$l={$item['location']}&w$l={$item['wins']}&k$l={$item['kills']}" );
     	if( $user['game_id'] && $is_open == 0 ) {
        printOut( "&r$l=".( $item['games'] > 4 ? "0" : "1") );
     	}
    	$l++;
    }

    printOut( "&err=0&eof" );
  } else {
    printOut( "err=1&eof" );
  }
