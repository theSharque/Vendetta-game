<?php
  include( 'db.inc' );
  global $user;

  if( auth() ) {
    $status = isset( $_GET['s'] ) ? $_GET['s'] : 1;
    db_safe( $status );


    if( $status == 1 ) {
        $imei = str_pad( $user['imei'], 16, '0000000000000000', STR_PAD_LEFT );
        db_query( "DELETE FROM queue WHERE imei = '$imei'" );

        $messages = db_fetch_array( "SELECT m.id, u.login, m.type, m.arg1, m.arg2
                                       FROM message m
                                 INNER JOIN users u ON u.id = m.sender_id
                                      WHERE m.user_id = {$user['id']} AND m.status = $status
                                      ORDER BY send_date DESC" );
    } else {
        $messages = db_fetch_array( "SELECT m.id, u.login, m.type, m.arg1, m.arg2
                                       FROM message m
                                 INNER JOIN users u ON u.id = m.sender_id
                                      WHERE m.type != 6 AND m.user_id = {$user['id']} AND m.status = $status
                                      ORDER BY send_date DESC" );
    }

    printOut( "cnt=".count( $messages ) );
    $l=0;
    foreach( $messages as $item ) {
      printOut( "&i$l={$item['id']}" );
      printOut( "&t$l={$item['type']}" );
      printOut( "&s$l={$item['login']}" );
      printOut( "&a$l={$item['arg1']}" );
      printOut( "&b$l={$item['arg2']}" );

      $l++;
    }
    printOut( "&err=0&eof" );

    if( $status == 1 ) {
      db_query( "DELETE FROM message WHERE user_id = {$user['id']} AND status = 0" );
      db_query( "UPDATE IGNORE message SET status = 2 WHERE user_id = {$user['id']} AND status = 1" );
      db_query( "DELETE FROM message WHERE user_id = {$user['id']} AND status = 1" );
    }
  } else {
    printOut( "err=1&eof" );
  }
