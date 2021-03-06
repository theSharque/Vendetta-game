<?php

  include( 'db.inc' );
  global $user;

  if( auth() ) {
    sendMessage( $user['id'], 0, 6, 0, "" );
    if( isset( $_GET['gt'] ) && is_numeric( $_GET['gt'] ) &&
        isset( $_GET['st'] ) && is_numeric( $_GET['st'] ) &&
        isset( $_GET['sc'] ) && is_numeric( $_GET['sc'] ) &&
        isset( $_GET['vc'] ) && is_numeric( $_GET['vc'] ) &&
        isset( $_GET['et'] ) && is_numeric( $_GET['et'] ) ) {

      $game_type = $_GET['gt'];
      $start_type = $_GET['st'];
      $start_count = $_GET['sc'];
      $victims_count = $_GET['vc'];
      $end_type = $_GET['et'];

      db_safe( $game_type );
      db_safe( $start_type );
      db_safe( $start_count );
      db_safe( $victim_count );
      db_safe( $end_type );

      if( $user['games'] < 5 ) {
        $game_type = 1;
      }

      if( $victim_count > $start_count - 1 && $start_type == 0 ) {
        $victim_count = $start_count - 1;
      }

      if( count( $user['location'] ) == 2 ) {
        $location = implode( ':', $user['location'] );
      } else {
        $location = '0:0';
      }

      db_query( "INSERT INTO games (      admin_id,  game_type,  start_type,  start_count,  victims_count,  end_type,    location )
                            VALUES ( {$user['id']}, $game_type, $start_type, $start_count, $victims_count, $end_type, '$location' )" );

      $id = mysql_insert_id();

      db_query( "UPDATE users SET game_id = $id WHERE id = {$user['id']}" );

      printOut( "err=0&eof" );
    } else {
      printOut( "err=2&eof" );
    }
  } else {
    printOut( "err=1&eof" );
  }
