FlightControls [![Build Status](http://img.shields.io/travis/dhleong/flight-controls.svg?style=flat)](https://travis-ci.org/dhleong/flight-controls)
==============

*Reach out and touch Flight Simulator X*

## What?

FlightControls is an open source Android app that connects 
directly to a running instance of Microsoft's Flight Simulator X, 
providing interactive controls that mimic those in the game. 
The graphics are not quite strictly following Android styling,
nor are they realistic, but they're functional; the point is not
so much the look, but the feel. Right now, the controls are modeled
more-or-less after the default Cessna (and some googling), but
alternate versions for Jet-types may come along later

### Features

- Nav/Com box, with inner/outer knobs
    - Tap the inner knob to toggle between 25 kHz and 50 kHz
- Transponder box
- Autopilot box with knob for altitude setting
    - Buttons to activate nav/heading modes work, but the LCD doesn't yet reflect them
- Heading indicator, with knob for the heading bug
- Analog Altimeter, with knob to adjust the Kohlsman setting (QNH)
- Light switches (with default Android widget... for now)
- Magneto switches as appropriate
- GPS panel
    - **Display is non-functioning**. This is mostly for a more 
    natural interface with the onscreen GPS *(for now)*

Along the way, it's also become a bit of a playground for experimenting
with Material Design and the new Design Support Library, including:

- RecyclerView with Swipe to Delete via ItemTouchHelper
- Snackbar + Floating Action Button via CoordinatorLayout
- Fullscreen FAB transition
- TextInputLayout

## Why?

I recently got into flying planes in 
[Flight Simulator X](http://store.steampowered.com/app/314160/)
and wanted a better way to interact with some of the switches and
instruments that I couldn't map onto my flight stick or throttle.

Also, I wanted to try out [Dagger 2](http://google.github.io/dagger/)
and the new [Design Support Library](http://android-developers.blogspot.com/2015/05/android-design-support-library.html)

## Special Thanks

- [jSimConnect 0.8](http://lc0277.gratisim.fr/jsimconnect.html) (LGPL)
