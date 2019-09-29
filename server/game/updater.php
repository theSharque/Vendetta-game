<?php

  include( 'db.inc' );
  global $user;

  if( auth() && isset( $_GET['u'] ) && is_numeric( $_GET['u'] )) {
  	$uid = $_GET['u'];
  	db_safe( $uid );

    $player = db_fetch_row( "SELECT u.location, u.die,  UNIX_TIMESTAMP( NOW() ) - UNIX_TIMESTAMP( u.ping_date ) ping
                               FROM victims v
                         INNER JOIN users u ON u.id = v.victim_id
                              WHERE u.id = $uid AND v.hunter_id = {$user['id']}" );

    printOut( "err=0" );
    printOut( "&l={$player['location']}" );
    printOut( "&d={$player['die']}" );
    printOut( "&p={$player['ping']}" );

    printOut( "&eof" );
  } else {
    printOut( "err=1&eof" );
  }
