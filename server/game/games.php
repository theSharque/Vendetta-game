<?php

  include( 'db.inc' );
  global $user;

  if( auth() ) {
    if( $user['games'] < 5 ) {
      $game_type = "game_type = 1 AND";
    } else {
      $game_type = "";
    }

    $games = db_fetch_array( "SELECT g.id, g.status, p.file, g.game_type, g.start_type, g.start_count, g.victims_count,
                                     g.end_type, g.start_date, u.location,
                                     IF( g.start_type = 0, count( u2.id ), TRUNCATE( ( UNIX_TIMESTAMP() - UNIX_TIMESTAMP( g.start_date ) ) / 60 / 60, 0 ) ) in_game
                                FROM games g
                          INNER JOIN users u ON u.id = g.admin_id
                          INNER JOIN users u2 ON u2.game_id = g.id
                          INNER JOIN photos p ON p.id = u.photo_id
                               WHERE ( $game_type g.status = 1 ) OR ( g.id = {$user['game_id']} )
                            GROUP BY g.id
                            ORDER BY IF( g.id = {$user['game_id']}, 0, 1 ), g.status, g.id" );

    printOut( "ig={$user['game_id']}&gs={$user['game_status']}" );

    $l=0;
    foreach( $games as $item ) {
      $from = explode( ':', $item['location'] );

      if( isset( $from[1] ) && isset( $user['location'][1] ) && distance( $from[0], $from[1], $user['location'][0], $user['location'][1] ) < 100000 ) {
        printOut( "&id$l={$item['id']}" );
        printOut( "&s$l={$item['status']}" );
        printOut( "&p$l={$item['file']}" );
        printOut( "&gt$l={$item['game_type']}" );
        printOut( "&st$l={$item['start_type']}" );
        printOut( "&sc$l={$item['start_count']}" );
        printOut( "&ig$l={$item['in_game']}" );
        printOut( "&vc$l={$item['victims_count']}" );
        printOut( "&et$l={$item['end_type']}" );
        $l++;
      }
    }

    printOut( "&err=0&cnt=$l&eof" );
  } else {
    printOut( "err=1&eof" );
  }
