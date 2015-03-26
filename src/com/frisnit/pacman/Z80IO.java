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
 * Very simple I/O implementation
 */
import net.sleepymouse.microprocessor.IBaseDevice;
 
public class Z80IO implements IBaseDevice
{
    Status status;
    
    public Z80IO(Status status)
    {
        this.status=status;
    }
    
    
    /**
     * Do nothing
     */
    @Override
    public int IORead(int address)
    {
        System.out.print("IORead called");
        return 0;
    }
 
    /**
     * Print a character
     */
    @Override
    public void IOWrite(int address, int data)
    {        
        System.out.println(String.format("IOWrite called at 0x%04x (0x%02x)",address,data));

        if(address==0x0000)
        {
            status.setIrqVector(data);
            System.out.println(String.format("IRQ set (0x%02x)",data));
        }
    }
}
