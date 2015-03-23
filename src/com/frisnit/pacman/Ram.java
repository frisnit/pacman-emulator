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
public class Ram {

    private static final int RAM_LENGTH = 0x1000;
    
    private int[] ram;
    
    public Ram()
    {
        ram = new int[RAM_LENGTH];    
    }

    public int readByte(int address)
    {
        if(address>=RAM_LENGTH)
        {
            System.out.println("RAM read out of bounds");
        }
       
        /*
        // simulate RAM fault
        if(address==0xc00)
            return 0xff;
        */
    //    System.out.println(String.format("RAM read from 0x%x",address));
        
        return ram[address] & 0xff;
    }

    public void writeByte(int address, int data)
    {
        data &= 0xff;
        
        if(address>=RAM_LENGTH)
        {
            System.out.println("RAM write out of bounds");
        }
    
        if(address<0x400)
        {
   //        System.out.println(String.format("RAM write to video RAM 0x%04x (0x%02x)",address,data));   
        }        
        else if(address<0x800)
        {
    //       System.out.println(String.format("RAM write to colour RAM 0x%04x (0x%02x)",address,data));   
        }        
        else if(address<0x1000)
        {
     //      System.out.println(String.format("RAM write to regular RAM 0x%04x (0x%02x)",address,data));   
        }
        else
        {
           System.out.println(String.format("RAM write to invalid RAM 0x%04x (0x%02x)",address,data));   
        }
            

        
        ram[address] = data;
    }
    /*
    public int readWord(int address)
    {
        return readByte(address) + readByte(address + 1)*256;
    }
    */
}
