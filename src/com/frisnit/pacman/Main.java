/* 
 * Copyright 2015 Mark Longstaff-Tyrrell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frisnit.pacman;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import net.sleepymouse.microprocessor.ProcessorException;
import net.sleepymouse.microprocessor.Z80.Z80Core;
 
public class Main implements KeyListener
{
    /**
     * Create CPU and run a program
     */
    public static void main(String[] args)
    {
        Main z80Demo = new Main();
        z80Demo.run();
    }
    
    Io io;
    Z80Memory memory;
    Z80Core z80;
    
    boolean paused = false;
    
    // Create CPU and loop through program
    public void run()
    {
        // move status to Io
        Status status = new Status();
        
        // create RAM
        Ram ram =  new Ram();
                
        // create memory mapped IO space
        io = new Io(status);

        // create address bus
        memory = new Z80Memory(status, ram, io);

        
        // create video emulator
        Video video = new Video(ram,io);
        
        // create sound emulator
        Sound sound = new Sound(io, status);

        // create a frame for the display
        JFrame frame = new JFrame("Z80 test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(video.getCanvas(), BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(Video.WIDTH*2+32, Video.HEIGHT*2+48));

        // get key events from the window
        video.getCanvas().addKeyListener(this);
        video.getCanvas().setFocusable(true);
        
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        //
        
        // create a Z80, add memory and IO handlers
        z80 = new Z80Core(memory, new Z80IO(status));
        z80.reset();
        
        while(!false)
        {
            try
            {
                // execute the next instruction
                z80.executeOneInstruction();
                 
                // run for 512000 clock cycles (1/60 second @ 3.072 MHz)
                if(z80.getTStates()>51200)
                {
                    z80.resetTStates();
                
                    do
                    {
                        try {
                            Thread.sleep(12);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    while(paused);
                    
                    if(status.isInterruptEnabled())
                    {
                        // fire INT (60Hz VBLANK interrupt)
                        // get the IRQ vector previously set by a write to IO address 0x0000
                        z80.setINT(status.getIntVector());

                        // draw screen
                        // emulate the video hardware
                        frame.repaint();

                        // make sounds
                        // emulate the sound hardware
                        sound.playSound();
                    }
                }
            }
            catch (ProcessorException e)
            {
                System.out.println("Hardware crash, oops! " + e.getMessage());
            }
        }
    }

    // input handling
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        io.keyPressed(e.getKeyCode());
        
        
       // System.out.println(String.format("Key pressed 0x%04x",e.getKeyCode()));

        // press 'r' to reset the CPU
        // leaves the RAM, ROM etc untouched        
        if(e.getKeyCode()==0x52)
        {
            System.out.println("Reset pressed");
            z80.reset();
        }

        // press 'p' to pause
        if(e.getKeyCode()==0x50)
        {
            paused=!paused;
            System.out.println(paused?"Paused":"Unpaused");
        }

        // press 'c' to apply cheat patch
        if(e.getKeyCode()==0x43)
        {
            System.out.println("Cheat patch applied");
            memory.cheatPatch();
        }
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        io.keyReleased(e.getKeyCode());
    }
}


