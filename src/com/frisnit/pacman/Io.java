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

/**
 *
 * @author mark
 */

/*

5000      interrupt enable
5001      sound enable
5002      ????
5003      flip screen
5004      1 player start lamp
5005      2 players start lamp
5006      coin lockout
5007      coin counter
5040-5044 sound voice 1 accumulator (nibbles) (used by the sound hardware only)
5045      sound voice 1 waveform (nibble)
5046-5049 sound voice 2 accumulator (nibbles) (used by the sound hardware only)
504a      sound voice 2 waveform (nibble)
504b-504e sound voice 3 accumulator (nibbles) (used by the sound hardware only)
504f      sound voice 3 waveform (nibble)
5050-5054 sound voice 1 frequency (nibbles)
5055      sound voice 1 volume (nibble)
5056-5059 sound voice 2 frequency (nibbles)
505a      sound voice 2 volume (nibble)
505b-505e sound voice 3 frequency (nibbles)
505f      sound voice 3 volume (nibble)
5060-506f Sprite coordinates, x/y pairs for 8 sprites
50c0      Watchdog reset

*/
public class Io {
    
    private boolean KEY_UP;
    private boolean KEY_DOWN;
    private boolean KEY_LEFT;
    private boolean KEY_RIGHT;

    private boolean KEY_COIN;
    private boolean KEY_P1_START;
    private boolean KEY_P2_START;
    
    private final Status status;
    
    // out
    private static final int WATCHDOG = 0x50c0;
    private static final int INT_ENABLE = 0x5000;
    private static final int SOUND_ENABLE = 0x5001;
    private static final int FLIP_SCREEN = 0x5003;

    // sound registers
    // implemented in the hardware as two 4-bit x 16 RAMs (74LS89)
    // arranged as 32 contiguous 4-bit words
    private static final int SOUND_START = 0x5040;
    private static final int SOUND_LENGTH = 0x20;
    final private int[] soundData; 

    // sprite registers
    // implemented in the hardware as two 4-bit x 16 RAM chips (74LS89)
    // arranged as 16 contiguous 8-bit words
    private static final int SPRITE_START = 0x5060;
    private static final int SPRITE_LENGTH = 0x10;
    final private int[] spriteData; 
    
    // inputs and DIP switches
    private static final int INPUTS_0 = 0x5000;
    private static final int INPUTS_1 = 0x5040;
    private static final int DIP_1 = 0x5080;
    private static final int DIP_2 = 0x50c0;

    
    public Io(Status status)
    {
        this.status=status;
        spriteData = new int[SPRITE_LENGTH];
        soundData = new int[SOUND_LENGTH];
    }
    
    /*
    
    IN0:
    01 up
    02 left
    04 right
    08 down
    10 rack test
    20 coin 1
    40 coin 2
    80 coin 3

IN1:
    01 up
    02 left
    04 right
    08 down
    10 service
    20 start 1
    40 start 2
    80 cabinet (set - upright)
    
    */
    public int readByte(int address)
    {
        int value;
        switch(address)
        {
            case INPUTS_0:
                value=0x10;
                value|=KEY_UP?0x00:0x01;
                value|=KEY_LEFT?0x00:0x02;
                value|=KEY_RIGHT?0x00:0x04;
                value|=KEY_DOWN?0x00:0x08;
                value|=KEY_COIN?0x00:0x20;
                                
                return value;
            
            case INPUTS_1:
                value=0x00;

                // P2 can use the same keys as P1
                value|=KEY_UP?0x00:0x01;
                value|=KEY_LEFT?0x00:0x02;
                value|=KEY_RIGHT?0x00:0x04;
                value|=KEY_DOWN?0x00:0x08;

                
                // service mode off
                value|=0x10;
                
                value|=KEY_P1_START?0x00:0x20;
                value|=KEY_P2_START?0x00:0x40;
  
                // upright mode
                //value |= 0x80;
                
                return value;
            
            case DIP_1:
                return 0xc9;

            case DIP_2:
                return 0xff;
            
            default:
                break;
        }
        return 0;
    }

    public void writeByte(int address, int data)
    {
        data&=0xff;
        
        
        
        switch(address)
        {
            case WATCHDOG:
              //  System.out.println(String.format("WATCHDOG 0x%02x",data));
                break;
                
            // enable/disable VBLANK 60Hz interrupt
            case INT_ENABLE:
               // System.out.println(String.format("INT_ENABLE 0x%02x",data));
                getStatus().setInterruptEnabled((data&0x01)==1);
                break;
                
            case SOUND_ENABLE:
                getStatus().setSoundEnabled((data&0x01)==1);
                System.out.println(getStatus().isSoundEnabled()?"Sound enabled":"Sound disabled");
                break;
                
            case FLIP_SCREEN:
                // video hardware should use this to flip the tiles for cocktail table mode
             //   System.out.println(String.format("FLIP_SCREEN 0x%02x",data));
                getStatus().setScreenFlipped((data&0x01)==1);
                break;
                
            default:
                break;
        }
        
        // record sprite data
        if(address>=SPRITE_START && address<SPRITE_START+SPRITE_LENGTH)
        {            
            //System.out.println(String.format("IO write: 0x%04x 0x%02x",address,data&0xff));
//data=0;
            
            spriteData[address-SPRITE_START]=data&0xff;
        }

        // record sound data
        if(address>=SOUND_START && address<SOUND_START+SOUND_LENGTH)
        {
            soundData[address-SOUND_START]=data&0xff;
        }
    }
    
    public int readSpriteData(int address)    
    {
        return spriteData[address];
    }

    // sound data is indexed as on the address bus
    // to make the lookups easier
    public int readSoundData(int address)    
    {
        return soundData[address-SOUND_START];
    }

    public void keyPressed(int keyCode)
    {
        switch(keyCode)
        {
            case 0x26://up
                KEY_UP=true;
                break;
            case 0x28://down
                KEY_DOWN=true;
                break;
            case 0x25://left
                KEY_LEFT=true;
                break;
            case 0x27://right
                KEY_RIGHT=true;
                break;
            case 0x35://coin (5)
                KEY_COIN=true;
                break;
            case 0x31:// p1start (1)
                KEY_P1_START=true;
                break;
            case 0x32:// p2start (2)
                KEY_P2_START=true;
                break;
        }
    }
    
    public void keyReleased(int keyCode)
    {
        switch(keyCode)
        {
            case 0x26://up
                KEY_UP=false;
                break;
            case 0x28://down
                KEY_DOWN=false;
                break;
            case 0x25://left
                KEY_LEFT=false;
                break;
            case 0x27://right
                KEY_RIGHT=false;
                break;
            case 0x35://coin (5)
                KEY_COIN=false;
                break;
            case 0x31:// p1start (1)
                KEY_P1_START=false;
                break;
           case 0x32:// p2start (2)
                KEY_P2_START=false;
                break;
        }  
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
}
