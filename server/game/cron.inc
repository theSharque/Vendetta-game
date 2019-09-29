<?php
  include( 'db.inc' );

  // Cleaner
  db_query( "DELETE FROM message
             WHERE status = 0 or
                 ( status = 1 AND send_date < DATE_SUB( NOW(), INTERVAL 3 HOUR ) ) OR
                 ( status = 2 AND send_date < DATE_SUB( NOW(), INTERVAL 24 HOUR ) )" );

  db_query( "DELETE FROM ingame WHERE old < DATE_SUB( NOW(), INTERVAL 7 DAY )" );
  db_query( "DELETE FROM ingame WHERE game_id NOT IN ( SELECT game_id FROM users )" );

  // Death message
  $dead = db_fetch_array( "SELECT u.id, u.login
                              FROM users u
                        INNER JOIN games g ON g.id = u.game_id
                             WHERE g.status = 2 AND ( u.ping_date IS NULL OR u.ping_date < DATE_SUB( NOW(), INTERVAL 60 MINUTE ) )" );
  foreach( $dead as $item ) {
    killPlayer( $item['id'], 3 );
  }

  // Pinger
  $users = db_fetch_array( "SELECT u.id
                              FROM users u
                         LEFT JOIN games g ON g.id = u.game_id
                             WHERE u.ping_date IS NULL OR
                                   u.ping_date < DATE_SUB( NOW(), INTERVAL 12 HOUR ) OR
                                 ( g.status = 1 AND u.ping_date < DATE_SUB( NOW(), INTERVAL 65 MINUTE ) ) OR
                                 ( g.status = 2 AND u.ping_date < DATE_SUB( NOW(), INTERVAL 7 MINUTE ) )" );
  foreach( $users as $item ) {
    if( $item['id'] > 0 ) {
      sendMessage( $item['id'], 0, 6, 0, "" );
    }
  }

  // BT updater
  $users = db_fetch_array( "SELECT u.id
                              FROM users u
                             WHERE u.bsid IS NULL OR u.bsid = ''" );
  foreach( $users as $item ) {
    if( $item['id'] > 0 ) {
      sendMessage( $item['id'], 0, 11, 0, "" );
    }
  }

/* Garbage collector */
  $games = db_fetch_array( "SELECT g.id, count( u.id ) users
                              FROM games g
                         LEFT JOIN users u ON u.game_id = g.id
                             WHERE g.status = 1 AND g.start_type = 1 AND g.start_date < DATE_SUB( NOW(), INTERVAL g.start_count HOUR )" );

  foreach( $games as $game ) {
    if( isset( $game['id'] ) ) {
      switch( $game['users'] ) {
        case 0:
          db_query( "DELETE FROM games WHERE id = {$game['id']}" );
          db_query( "DELETE FROM victims WHERE game_id = {$game['id']}" );
          break;

        case 1:
          db_query( "DELETE FROM games WHERE id = {$game['id']}" );
          db_query( "DELETE FROM victims WHERE game_id = {$game['id']}" );
          $player = db_fetch_row( "SELECT id FROM users WHERE game_id = {$game['id']}" );
          sendMessage( $player['id'], 0, 4, 0, "" );
          break;

        default:
          startGame( $game['id'] );
          break;
      }
    }
  }

  $games = db_fetch_array( "SELECT g.id, count( u.id ) users
                              FROM games g
                         LEFT JOIN users u ON u.game_id = g.id
                          GROUP BY g.id
                            HAVING id IS NOT NULL AND users = 0" );

  foreach( $games as $game ) {
    db_query( "DELETE FROM games WHERE id = {$game['id']}" );
  }

  db_query( "UPDATE users SET game_id = 0 WHERE game_id NOT IN ( SELECT id FROM games )" );

  $photos = scandir( '/var/www/vendetta/game/photos' );
  foreach( $photos as $file ) {
    if( $file != '.' && $file != '..' ) {
      $fld = substr( $file, 0, -4 );

      $used = db_fetch_val( "SELECT id FROM photos WHERE file = '$fld'", 'id' );
      if( !$used ) {
        unlink( '/var/www/vendetta/game/photos/'.$file );
      }
    }
  }

  db_query( "OPTIMIZE TABLE games, gcm, ingame, message, photos, users, victims" );
