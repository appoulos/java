
// from: https://www.patater.com/gbaguy/javamidi.htm
import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.*;
import javax.sound.midi.*;

class MidiPlayNote {
	static Receiver rcvr;
	static Synthesizer synth = null;

	public static void main(String[] args) {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
		} catch (Exception e) {
			System.exit(1);
		}

		// MidiSystem is one of those "Don't need to instance it" objects, and here I
		// use MidiSystem
		// to get a fully working (almost) synthesizer. After we get the Synthesizer we
		// need to open,
		// why? I dunno, but you have to or it won't work!
		//
		// Next, we get an array of MidiChannels which is what we'll actually use later
		// to make a sound.

		final MidiChannel[] mc = synth.getChannels();

		// I declare mc as final because we need to be able to access it without
		// problems
		// in the JButton's ActionListener later. So far this is straight forward and I
		// think it
		// doesn't get much harder than a bunch of method calls with several objects and
		// interfaces.
		//
		// Now, we want an array of Instruments to choose from. To do this we have to
		// get a
		// Soundbank object, I just use the default one with this line o' code:

		Instrument[] instr = synth.getDefaultSoundbank().getInstruments();

		// Instruments are used to choose what instrument the sound is played with.

		synth.loadInstrument(instr[90]);

		// apoulos timing:
		ShortMessage myMsg = new ShortMessage();
		// Start playing the note Middle C (60),
		// moderately loud (velocity = 93).
		try {
			myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
		} catch (Exception e) {
			System.exit(1);
		}

		// long timeStamp = 1000000;

		System.out.println("usec position: " + synth.getMicrosecondPosition());
		try {
			rcvr = MidiSystem.getReceiver();
		} catch (Exception e) {
			System.exit(1);
		}

		// rcvr.send(myMsg, timeStamp);

		// Here I load into our Synthesizer, an arbitrary instrument from the Instrument
		// array
		// that we just made.

		// The Swing Window

		// In this section we construct a simple Swing JFrame with a single JButton
		// on it. Simple stuff so far, eh?

		// Now, for the Swing window, I'm just going to give you the 8 line chunk of
		// code
		// that creates a JFrame and puts a JButton inside. This tutorial will NOT
		// explain how to make JFC/Swing GUI applications, I'm only 15 and I know Swing
		// fairly well, I'm
		// sure you can too.

		// Here it is:

		JFrame frame = new JFrame("Sound1");
		JPanel pane = new JPanel();
		JButton button1 = new JButton("Click me!");
		frame.getContentPane().add(pane);
		Dimension d = new Dimension(200, 200);
		frame.setPreferredSize(d);
		frame.setMinimumSize(d);
		frame.setMaximumSize(d);
		// frame.setPreferredSize(new Dimension(200, 200));
		pane.add(button1);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		// frame.show();

		// That makes the window and shows it.
		// That also is the end of this section.

		// Making a Sound

		// In this section we make an ActionListener for our JButton and put
		// inside ONE (1) line of code to make a sound.

		// Here's the start of the JButton's ActionListener as usual this is pretty
		// straight forward:

		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Now, ladies and gentleman the code you've all been waiting for:

				// mc[5].noteOn(50, 600);
				// try {
				// mc[5].wait(1000, 0);
				// } catch (Exception _) {
				// }
				// mc[5].noteOn(60, 600);

				long d = synth.getMicrosecondPosition();
				rcvr.send(myMsg, 1000000 + d);
				rcvr.send(myMsg, 1100000 + d);
				// rcvr.send(myMsg, 1000000 + synth.getMicrosecondPosition());
				// rcvr.send(myMsg, 1100000);

				// ShortMessage myMsg = new ShortMessage();
				// try {
				// myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
				// long timeStamp = 1000000;
				// Receiver rcvr = MidiSystem.getReceiver();
				// rcvr.send(myMsg, timeStamp);
				// timeStamp = 1100000;
				// rcvr.send(myMsg, timeStamp);
				// } catch (Exception f) {
				// System.out.println(f);
				// }

				// That makes the sound in an arbitrary MidiChannel that I picked (THE STANDARD
				// AMOUNT OF
				// MIDI CHANNELS IS 16 SO YA KNOW!) I don't know the difference of the Midi
				// Channels.
				// To make the sound we just turn on a note with the noteOn method. The 60 is
				// the note number (middle C) and 600 is how hard we hit the piano key or
				// plucked the
				// instrument's string (you get the idea).

				// THERE IS A noteOff METHOD BUT AS FAR AS I CAN TELL THE SOUND DIES OFF ALL BY
				// ITSELF!!!

				// Here's some finish up code:

			}
		}); // END OF THE ACTION LISTENER
	} // END OF THE MAIN METHOD
}
