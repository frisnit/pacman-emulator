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

import java.util.HashMap;
import net.sleepymouse.microprocessor.IMemory;
 
public class Z80Memory implements IMemory
{
    private Rom rom;
    private Ram ram;
    private Io  io;
    
    public Z80Memory(Status status, Ram ram, Io io, String rompack)
    {
        this.io=io;
        this.ram = ram;

        // load program rom images
        HashMap<String, Integer> romList = new HashMap<String, Integer>();

        romList.put("pacman.6e", 0x0000);
        romList.put("pacman.6f", 0x1000);
        romList.put("pacman.6h", 0x2000);
        romList.put("pacman.6j", 0x3000);
        rom = new Rom(0x4000,0x1000, romList, rompack);

    }

    private final static int ROM_START    = 0x0000;
    private final static int ROM_LENGTH   = 0x4000;
    
    private final static int RAM_START    = 0x4000;
    private final static int RAM_LENGTH   = 0x1000;

    private final static int IO_START    = 0x5000;
    private final static int IO_LENGTH   = 0x100;

    private final static int TYPE_ROM     = 0;
    private final static int TYPE_RAM     = 1;
    private final static int TYPE_IO      = 2;
    private final static int TYPE_INVALID = 3;
    

    // return memory offset from bus address
    private int addressOffset(int address, int type)
    {        
        switch(type)
        {
            case TYPE_ROM:
                address-=ROM_START;
                break;
            case TYPE_RAM:
                address-=RAM_START;
                break;
            case TYPE_IO:
                break;
            default:
                break;
        }
        
        return address;
    }

    
    // return memory type from bus address
    private int addressDecode(int address)
    {
        if(address>=ROM_START && address<ROM_START+ROM_LENGTH)
        {
            return TYPE_ROM;
        }

        if(address>=RAM_START && address<RAM_START+RAM_LENGTH)
        {            
            return TYPE_RAM;
        }
        
        if(address>=IO_START && address<IO_START+IO_LENGTH)
        {
            return TYPE_IO;
        }
        
        return TYPE_INVALID;
    }

    @Override
    // Read a byte from meory
    public int readByte(int address)
    {
        // only lines A0 to A14 are connected
        address &=0x7fff;
        
       // System.out.println(String.format("Read at 0x%x",address));
        
        int type    = addressDecode(address);
        int offset  = addressOffset(address,type);

        switch(type)
        {
            case TYPE_ROM:
                // note only lines A0 to A14 connected to ROMs
                return rom.readByte(offset);
            case TYPE_RAM:
                // note only lines A0 to A11 connected to RAMs
                return ram.readByte(offset);
            case TYPE_IO:
                int value = io.readByte(offset);
                //System.out.println(String.format("IO read 0x%02x from 0x%x",value,address));
                return value;
            default:
                System.out.println(String.format("Invalid read at 0x%x",address));
                break;
        }   
        
        return 0;

    }
 
    @Override
    // Read a word from memory
    public int readWord(int address)
    {
        return readByte(address) + readByte(address + 1)*256;
    }
 
    @Override
    public void writeByte(int address, int data)
    {
        // no A15 line connected in pacman hardware
        address &=0x7fff;

        int type    = addressDecode(address);
        int offset  = addressOffset(address,type);
        
        switch(type)
        {
            case TYPE_ROM:
                System.out.println(String.format("Write attempt to ROM at 0x%04x",address));
                break;
            case TYPE_RAM:
                ram.writeByte(offset,data);
                break;
            case TYPE_IO:
                //System.out.println(String.format("Wrote 0x%02x to IO address at 0x%04x",data,address));
                io.writeByte(offset,data);                
                break;
            default:
                System.out.println(String.format("Read to unmapped address at 0x%04x",address));
                break;
        }
    }
 
    @Override
    public void writeWord(int address, int data)
    {
        byte low = (byte)(data & 0xff);
        byte high = (byte)((data>>8) & 0xff);
        
        writeByte(address,low);
        writeByte(address+1,high);
    }

    public void cheatPatch()
    {
        // patch ROM for invincibility
        
        rom.writeByte(0x1774,0xc3);
        rom.writeByte(0x1775,0xe0);
        rom.writeByte(0x1776,0x3c);
        rom.writeByte(0x1777,0x00);

        rom.writeByte(0x3ce0,0xa7);
        rom.writeByte(0x3ce1,0x20);
        rom.writeByte(0x3ce2,0x04);
        rom.writeByte(0x3ce3,0x00);

        rom.writeByte(0x3ce3,0xaf);
        rom.writeByte(0x3ce4,0xc3);
        rom.writeByte(0x3ce5,0x64);
        rom.writeByte(0x3ce6,0x17);

        rom.writeByte(0x3ce7,0xaf);
        rom.writeByte(0x3ce8,0xc3);
        rom.writeByte(0x3ce9,0x77);
        rom.writeByte(0x3cea,0x17);
  
    }

}