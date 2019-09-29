<?php

  include( 'db.inc' );
  global $user;

  if( isset( $_GET['b'] ) && isset( $_GET['l'] ) && isset( $_GET['p'] ) && strlen( $_GET['l'] ) ) {
    $imei = $_GET['b'];
    $login = mb_convert_encoding( $_GET['l'], 'Windows-1251', 'UTF-8' );

    db_safe( $imei );
    db_safe( $login );

    if( !auth() ) {
      $phone = str_replace( array( " ", "-", "+" ), "", $_GET['p'] );

      db_safe( $phone );

      $phone = substr( $phone, -9 );

      db_query( "INSERT INTO users ( login, imei, phone ) VALUES ( '$login', '$imei', '$phone' )" );
      $id = mysql_insert_id();
      sendMessage( $id, 0, 6, 0, "" );
    } else {
      db_query( "UPDATE users SET login = '$login' WHERE imei = '$imei'" );
    }

    printOut( "err=0&eof" );
  } else {
    printOut( "err=1&eof" );
  }
