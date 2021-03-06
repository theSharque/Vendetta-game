<?php

  include( 'db.inc' );
  global $user;

  if( auth() && isset( $_GET['g'] ) && is_numeric( $_GET['g'] ) && isset( $_GET['p'] ) && is_numeric( $_GET['p'] ) ) {
    $gid = $_GET['g'];
    $player = $_GET['p'];

    db_safe( $gid );
    db_safe( $player );

    $players = db_fetch_array( "SELECT u.id, u.login, i.status, p.file, IF( u.id = g.admin_id, 1, 0 ) admin, u.location, u.wins, u.kills,
                                       IFNULL( i.kills, 0 ) stars, IFNULL( u2.login, 'ERR') killer
                                  FROM ingame i
                             LEFT JOIN users u ON u.id = i.user_id
                             LEFT JOIN games g ON g.id = i.game_id
                            INNER JOIN photos p ON p.status = 2 AND p.user_id = u.id
                             LEFT JOIN users u2 ON u2.id = i.killer_id
                                 WHERE i.game_id = $gid
                              ORDER BY IF( u.id = $player, 0, 1 ), admin DESC, i.status DESC, u.id" );

    printOut( "err=0&cnt=".count( $players ) );

    $admin = db_fetch_row( "SELECT admin_id, status FROM games WHERE id = $gid" );

    printOut( "&ga=".( $admin['admin_id'] == $user['id'] ? 1 : 0 ) );
    printOut( "&gt=".$admin['status'] );

    $l=0;
    foreach( $players as $item ) {
      printOut( "&id$l={$item['id']}" );
      printOut( "&p$l={$item['file']}" );
      printOut( "&l$l={$item['login']}" );
      printOut( "&a$l={$item['admin']}" );
      printOut( "&g$l={$item['location']}" );
      printOut( "&w$l={$item['wins']}" );
      printOut( "&k$l={$item['kills']}" );
      printOut( "&d$l={$item['status']}" );
      printOut( "&s$l={$item['stars']}" );
      printOut( "&kb$l={$item['killer']}" );

      $l++;
    }

    printOut( "&eof" );
  } else {
    printOut( "err=1&eof" );
  }
