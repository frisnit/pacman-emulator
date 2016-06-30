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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mark
 */
public class Rom {    
    
    private final int romSize;    
    private final int chipSize;    
    private final HashMap<String, Integer> romConfig;
    private int[] rom;
    
    public Rom(int romSize, int chipSize, HashMap<String, Integer> romConfig, String filename)
    {    
        this.romConfig = romConfig;
        this.romSize=romSize;
        this.chipSize=chipSize;
        
        rom = new int[romSize];
        
        // load ROMs from zip file
        loadROMS(filename);   
    }

    private int loadROMS(String filename)
    {
        try
        {
            ZipFile zipFile = new ZipFile(filename);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while(entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                Integer value = romConfig.get(entry.getName());
                
                if(value!=null)
                {
                    int offset = value;
                    
                    byte[] data = new byte[chipSize];
                    stream.read(data, 0, chipSize);
                    
                    String hash = sha1(data);

                    System.out.println(String.format("Loading: %s Start 0x%04x End 0x%04x SHA1 %s",entry.getName(),offset,offset+chipSize-1,hash));

                    // load ROM into ROM array
                    for(int n=offset;n<offset+chipSize;n++)
                    {
                        rom[n]=data[n-offset];
                    }
                }
            }
        }
        catch(IOException i)
        {
            System.out.println(i.getMessage());
            return -1;
        }
        
        return 0;
    }
    
    static String sha1(byte[] input)
    {
        try
        {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");

            byte[] result = mDigest.digest(input);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            return "Not available";
        }
    }
    
    public int readByte(int address)
    {
        if(address>=romSize)
        {
            System.out.println(String.format("ROM read out of bounds at 0x%04x",address));
        }

        return rom[address] & 0xff;
    }    

    // for patching ROMs
    public void writeByte(int address, int data)
    {
        data &= 0xff;
        
        if(address>=romSize)
        {
            System.out.println("ROM write out of bounds");
        }
        
        rom[address] = data;
    }
}
