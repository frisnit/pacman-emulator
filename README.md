# pacman-emulator
A Pac Man machine emulator written in Java

![alt tag](https://github.com/frisnit/pacman-emulator/blob/master/screenshot.png)

This emulator was written with the help of Chris Lomont's Pac Man emulation guide:

http://www.lomont.org/Software/Games/PacMan/PacmanEmulation.pdf

Take a look at that for full details of the implementation.

There are six main parts to the emulator:

## CPU
This is based on the Java Z80 microprocessor emulator that can be found here: https://code.google.com/p/z80-cpu/ with additions to support the mode 2 external interrupt (INT) required by the machine's sound and display hardware. Pressing 'r' while the emulator is running will force a CPU reset.

## Memory
This emulates the CPU address bus (Z80Memory.java) and provides the address mappings to the program ROM (0x0000 to 0x3fff), RAM (0x4000 to 0x4fff) and memory mapped IO space (0x5000 to 0x50ff) which is used to control the sound, sprites and player controls.

## IO
Not to be confused with the memory mapped IO described above, this emulates the Z80 peripheral hardware IO bus (Z80IO.java). In this emulation this performs only one function: to store the INT vector for use during the 60Hz VBLANK interrupt. 

## Video
This emulates the video hardware and produces a representation of the screen based on the state of the video RAM and sprite registers. It runs 60 times a second. The Pac Man screen consists of an array of static tiles overlaid with sprites. The tiles are stored in a character based array in RAM alongside the corresponding palette entries for each tile. The sprite hardware is completely separate from the CPU and is controlled by the sprite registers in the memory mapped IO space. The hardware can draw up to eight 16x16 pixel sprites anywhere on the screen. The tile layout, tile/sprite image format and colour lookup is completely bonkers. See Chris's PDF for a full explanation. Maybe it makes more sense when you look at the actual hardware. See Video.java for the implementation.

## Sound
The Pac Man sound hardware supports 3 voices on a single mono channel. Each of these voices can select the volume and frequency of one of 8 output waveforms. The sound registers are written to by the CPU over the memory mapped IO space during the 60Hz VBLANK interrupt. The Pac Man sound hardware runs at a 96kHz sample rate so the output from the sound emulator is downsampled to 44.1kHz for output. It currently doesn't low pass filter the 96kHz stream though so there could be aliasing in the output. See Sound.java for implementation.

## Inputs
This emulation currently supports the Player 1 controls (cursor keys), start button (1) and coin input (5). The DIP switches are hardcoded. See Io.java for implementation.

---

### A note about ROMs
You'll need to get a set of Pac Man ROMs from somewhere else to make this work. Put the following ROM images in a file called pacman.zip in the project root directory and they'll get picked up when the machine starts:

    pacman.5e  SHA1 06ef227747a440831c9a3a613b76693d52a2f0a9
    pacman.5f  SHA1 4a937ac02216ea8c96477d4a15522070507fb599  
    82s123.7f  SHA1 8d0268dee78e47c712202b0ec4f1f51109b1f2a5  
    82s126.4a  SHA1 19097b5f60d1030f8b82d9f1d3a241f93e5c75d6  
    82s126.1m  SHA1 bbcec0570aeceb582ff8238a4bc8546a23430081  
    82s126.3m  SHA1 0c4d0bee858b97632411c440bea6948a74759746  
    pacman.6e  SHA1 e87e059c5be45753f7e9f33dff851f16d6751181  
    pacman.6f  SHA1 674d3a7f00d8be5e38b1fdc208ebef5a92d38329  
    pacman.6h  SHA1 8e47e8c2c4d6117d174cdac150392042d3e0a881  
    pacman.6j  SHA1 d4a70d56bb01d27d094d73db8667ffb00ca69cb9 

The emulator doesn't check the hashes, it'll try and run anything you give it (including no ROMs at all). These are just given here as a first line in sanity checking your setup.

