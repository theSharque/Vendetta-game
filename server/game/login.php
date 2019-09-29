<?php

  include( 'db.inc' );
  global $user;

  if( auth() ) {
    printOut( "err=0&id={$user['id']}&login={$user['login']}&ig={$user['game_id']}&gs={$user['game_status']}" );
    printOut( "&vp={$user['vpass']}&va={$user['vans']}&pn={$user['phone']}&gm={$user['games']}" );
    printOut( "&kl={$user['kills']}&wn={$user['wins']}&die={$user['die']}&msg={$user['messages']}" );
    printOut( "&photo={$user['photo_url']}&na={$user['approve']}&eof" );
  } else {
    printOut( "err=1&eof" );
  }
