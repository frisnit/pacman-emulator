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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author mark
 */

/*

224 x 288 pixels
28 x 36 tiles

256 8x8 pixel tiles
64 16x16 pixel sprites

6MHz pixel clock (scans vertically - screen is rotated through 90 degrees)

32 colours, only 16 used
64 palettes, 4 bytes (colours) each

*/

public class Video {
    
    public static final int WIDTH = 224;
    public static final int HEIGHT = 288;
   
    BufferedImage bitmap;
        
    Rom tileRom;
    Rom spriteRom;
    Rom colourRom;
    Rom paletteRom;
    
    // for getting sprite data
    Io io;
    
    // for getting video and sprite data
    Ram ram;
    
    //PaletteEntry[] palettes; 
    
    private int[][] tiles;
    
  

    public Video(Ram ram, Io io, String rompack)
    {
        this.io=io;
        this.ram=ram;

        // load tile ROM
        HashMap<String, Integer> romList = new HashMap<String, Integer>();
        romList.put("pacman.5e", 0x0000);
        tileRom = new Rom(0x1000,0x1000, romList, rompack);
        
        // load sprite ROM
        romList = new HashMap<String, Integer>();
        romList.put("pacman.5f", 0x0000);
        spriteRom = new Rom(0x1000,0x1000, romList, rompack);        
        
        // load colour ROM
        romList = new HashMap<String, Integer>();
        romList.put("82s123.7f", 0x0000);
        colourRom = new Rom(0x20,0x20, romList, rompack);        

        // load palette ROM
        romList = new HashMap<String, Integer>();
        romList.put("82s126.4a", 0x0000);
        paletteRom = new Rom(0x100,0x100, romList, rompack);        
        
        bitmap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        canvas = new DrawCanvas();
       
        tiles = new int[256][];
        // unpack the 256 tiles
        for(int i=0;i<256;i++)
        {
            int[] characterData = new int[64];

            // 16 bytes per char
            for(int n=0;n<16;n++)
            {
                int address = i*16+n;
                                
               // address^=0x03ff;
                
                // simulate two address pins shorted
               // address|=((address&0x08)>>1);
               // address|=((address&0x04)<<1);
                
                // get next byte of character
                int characterByte = tileRom.readByte(address);
/*
                // simulate a data line floating
                if(Math.random()>0.5)
                    characterByte&=0xfe;
                else
                    characterByte|=0x01;
*/
                
                // 4 pixels per byte 
                characterData[n*4+0]=((characterByte>>0)&0x01)|((characterByte>>3)&0x02);
                characterData[n*4+1]=((characterByte>>1)&0x01)|((characterByte>>4)&0x02);
                characterData[n*4+2]=((characterByte>>2)&0x01)|((characterByte>>5)&0x02);
                characterData[n*4+3]=((characterByte>>3)&0x01)|((characterByte>>6)&0x02);
            }
            
            tiles[i]=characterData;
        }

        
        // TODO: precalculate the 32 palette colours
      /*
        palettes = new PaletteEntry[64];
        
        for(int n=0;n<64;n++)
        {
            palettes[n] = getTilePalette(n);
        }
        */
        // TODO: unpack the tile bitmaps?
        // TODO: unpack the sprite bitmaps?
    }    
    

    
    private DrawCanvas canvas;

    /**
     * @return the canvas
     */
    public DrawCanvas getCanvas() {
        return canvas;
    }
    
    // hold the colour values for this palette entry
    private class PaletteEntry
    {
        private Color[] colours = new Color[4];

        /**
         * @return the colours
         */
        public Color getColour(int index) {
            return colours[index];
        }
        
        /*
            Bit Color Connected to resistor Weight
            0 Red (Least amount) 1000 ohm 21h
            1 Red 470 ohm 47h
            2 Red (Most amount) 220 ohm 97h
            3 Green (Least amount) 1000 ohm 21h
            4 Green 470 ohm 47h
            5 Green (Most amount) 220 ohm 97h
            6 Blue 470 ohm 51h
            7 Blue 220 ohm AEh
        */

        /**
         * @param colours the colours to set
         */
        public void setColour(int colour, int index)
        {
            // black is transparent
            if(colour==0x00)
            {
                this.colours[index] = new Color(0,0,0,0);
                return;
            }
            
            int r,g,b;
            
            // create RGB colour from byte from colour ROM
            r = ((colour>>0)&0x01)*0x21 + ((colour>>1)&0x01)*0x47 + ((colour>>2)&0x01)*0x97;
            g = ((colour>>3)&0x01)*0x21 + ((colour>>4)&0x01)*0x47 + ((colour>>5)&0x01)*0x97;
            b = ((colour>>6)&0x01)*0x51 + ((colour>>7)&0x01)*0xae;
            
            this.colours[index] = new Color(r,g,b);
        }
    }
        
    
    
    public BufferedImage getBitmap()
    {
        return bitmap;
    }
    
    public class DrawCanvas extends JPanel {
      // Override paintComponent to perform your own painting
      @Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g);     // paint parent's background
         setBackground(Color.BLACK);  // set background color for this JPanel
         renderFrame();
         
         g.drawImage(bitmap,16,16,Video.WIDTH*2, Video.HEIGHT*2,null);
       }
   }

    // draw the tiles in this frame, untangling the strange tile layout
    public void renderFrame()
    {
        Graphics2D g2 = bitmap.createGraphics();
     
        g2.setColor(Color.BLACK);
        g2.fillRect(0,0, WIDTH, HEIGHT);
        
        // draw the tiles
        renderTiles();
        
        // flip tile screen if necessary for player 2 in cocktail mode
        // (this is a massive shortcut in the emulation!)
        if(io.getStatus().isScreenFlipped())
        {
            // rotate image through 180 degrees (flip both x and y)
            AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
            tx.translate(-bitmap.getWidth(null), -bitmap.getHeight(null));
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            bitmap = op.filter(bitmap, null);
        }

        // draw sprites on top of the tiles
        renderSprites();
    }
    
    // return an 8x8 image of the tile 'value' with colour at colour RAM location 'offset' 
    BufferedImage getCharacter(int offset, int value)
    {
        BufferedImage character = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = character.createGraphics();
     
        
        /*
            Bit Usage
            0 Bit 0 of pixel #1
            1 Bit 0 of pixel #2
            2 Bit 0 of pixel #3
            3 Bit 0 of pixel #4
            4 Bit 1 of pixel #1
            5 Bit 1 of pixel #2
            6 Bit 1 of pixel #3
            7 Bit 1 of pixel #4
        */
        
        /*
        int[] characterData = new int[64];
        // decode char
        
        // 16 bytes per char
        for(int n=0;n<16;n++)
        {
            // get next byte of character
            int characterByte = tileRom.readByte(value*16+n);
            
            // 4 pixels per byte 
            characterData[n*4+0]=((characterByte>>0)&0x01)|((characterByte>>3)&0x02);
            characterData[n*4+1]=((characterByte>>1)&0x01)|((characterByte>>4)&0x02);
            characterData[n*4+2]=((characterByte>>2)&0x01)|((characterByte>>5)&0x02);
            characterData[n*4+3]=((characterByte>>3)&0x01)|((characterByte>>6)&0x02);
        }
*/
        
        int[] characterData = tiles[value];
        
        // get colour value from colour RAM
        int paletteIndex = ram.readByte(offset+0x0400)&0xff;
        
        // get the 4 colour palette for this tile
        //PaletteEntry palette = palettes[paletteIndex];//getTilePalette(paletteIndex);
        PaletteEntry palette = getTilePalette(paletteIndex);
        
        //g2.drawString(String.format("%c",value),0,0);
        //g2.setColor(palette.getColour(1));
        //g2.fillRect(0,0, 7, 7);
        
        
        // draw the two tile slices
        drawSpriteSlice(palette, character, characterData, 0, 0, 4);
        drawSpriteSlice(palette, character, characterData, 1, 0, 0);
       
        return character;
    }
    
    void renderTiles()
    {
         Graphics2D g2 = bitmap.createGraphics();

         // render bottom two lines
        for(int n=0x02;n<0x1e;n++)
        {
            int value = ram.readByte(n);
            BufferedImage character = getCharacter(n, value);
            
            // draw char on screen
            g2.drawImage(character, 8*(27-n+2),34*8,null);            
        }
        for(int n=0x22;n<0x3e;n++)
        {
            int value = ram.readByte(n);
            BufferedImage character = getCharacter(n, value);
            
            // draw char on screen
            g2.drawImage(character, 8*(27-n+0x22),35*8,null);
        }
        
        // render main area
        for(int n=0x40;n<0x3c0;n++)
        {
            int value = ram.readByte(n);
            BufferedImage character = getCharacter(n, value);
            
            int offset = n-0x40;
            
            int x=27-offset/0x20;
            int y=offset%0x20+2;
            
            // draw char on screen
            g2.drawImage(character, x*8,y*8,null);
        }

        // render top two lines
        for(int n=0x3c2;n<0x3e0;n++)
        {
            int value = ram.readByte(n);
            BufferedImage character = getCharacter(n, value);
            
            // draw char on screen
            g2.drawImage(character, 8*(27-n+0x3c2),0,null);            
        }
        for(int n=0x3e2;n<0x400;n++)
        {
            int value = ram.readByte(n);
            BufferedImage character = getCharacter(n, value);
            
            // draw char on screen
            g2.drawImage(character, 8*(27-n+0x3e2),8,null);
        }        
         
    }
            
    // return palette at this offset
    PaletteEntry getTilePalette(int paletteIndex)
    {        
        // look up colours in colour ROM and store them in the palette object
        PaletteEntry palette = new PaletteEntry();

        paletteIndex<<=2;// offset into palette ROM (4 bytes per palette)

        // palette ROM has  address lines (lowest two lines are the pixel colour value)
        paletteIndex&=0xff;
        
        int colourIndex = paletteRom.readByte(paletteIndex);
        palette.setColour(colourRom.readByte(colourIndex),0);

        colourIndex = paletteRom.readByte(paletteIndex+1);
        palette.setColour(colourRom.readByte(colourIndex),1);

        colourIndex = paletteRom.readByte(paletteIndex+2);
        palette.setColour(colourRom.readByte(colourIndex),2);

        colourIndex = paletteRom.readByte(paletteIndex+3);
        palette.setColour(colourRom.readByte(colourIndex),3);
        
        return palette;
    }
    
    /*
    
        Sprite X-location Y-location Sprite#, flip-x, flip-y Palette
        0 5060h 5061h 4FF0h 4FF1h
        1 5062h 5063h 4FF2h 4FF3h
        2 5064h 5065h 4FF4h 4FF5h
        3 5066h 5067h 4FF6h 4FF7h
        4 5068h 5069h 4FF8h 4FF9h
        5 506Ah 506Bh 4FFAh 4FFBh
        6 506Ch 506Dh 4FFCh 4FFDh
        7 506Eh 506Fh 4FFEh 4FFFh

    */
    
    private void renderSprites()
    {
        Graphics2D g = bitmap.createGraphics();
        
       // BufferedImage sprite = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
               
        // for each sprite
        for(int n=7;n>=0;n--)
        {
            // get sprite x, y position
            // screen origin is lower right, the opposite to the bitmap
            //  sprites aren't drawn on top two or bottom two tile rows
            int x = WIDTH-io.readSpriteData(n*2)+15;
            int y = HEIGHT-io.readSpriteData(n*2+1)-16;
        
            // get palette index
            int spriteColour = ram.readByte(0x0FF1+n*2)&0xff;
            
            // get sprite index + rotation
            int spriteIndexAndRotation = ram.readByte(0x0FF0+n*2);
                        
            BufferedImage spriteImage = getSprite( spriteIndexAndRotation, spriteColour);
            
//System.out.println(String.format("Sprite %d colour %d : %s", n, spriteColour, palette.getColour(1).toString()));

            // draw a grey box around the sprites
            //Graphics2D spriteGraphics = spriteImage.createGraphics();
            //spriteGraphics.setColor(Color.GRAY);
            //spriteGraphics.drawRect(0,0,15,15);
            
            // rotate sprite if necessary
            
            // no rotation
            if((spriteIndexAndRotation&0x03)==0x00)
                g.drawImage(spriteImage,x,y,16,16,null);
            
            // flip X
            if((spriteIndexAndRotation&0x03)==0x02)
                g.drawImage(spriteImage,x+16,y,-16,16,null);
            
            // flip Y
            if((spriteIndexAndRotation&0x03)==0x01)
                g.drawImage(spriteImage,x,y+16,16,-16,null);
            
            // flip X and Y
            if((spriteIndexAndRotation&0x03)==0x03)
                g.drawImage(spriteImage,x+16,y+16,-16,-16,null);
            
        }
    }
 
    
    // return an 16x16 image of the sprite at 'spriteIndex' with colours from palette 'paletteIndex' 
    BufferedImage getSprite(int spriteIndexAndRotation, int paletteIndex)
    {
        BufferedImage character = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        //Graphics2D g2 = character.createGraphics();
               
        /*
            Bit Usage
            0 Bit 0 of pixel #1
            1 Bit 0 of pixel #2
            2 Bit 0 of pixel #3
            3 Bit 0 of pixel #4
            4 Bit 1 of pixel #1
            5 Bit 1 of pixel #2
            6 Bit 1 of pixel #3
            7 Bit 1 of pixel #4
        */
        
        int spriteIndex = (spriteIndexAndRotation>>2)&0xff;
        //int rotation = spriteIndexAndRotation&0x03;
        
        int[] characterData = new int[256];
        
        // 64 bytes per char
        for(int n=0;n<64;n++)
        {
            // get next byte of character
            int address = spriteIndex*64+n;
           
            // simulate an address line stuck high
            //address|=0x0001;

            int characterByte = spriteRom.readByte(address);
            
            // 4 pixels per byte 
            characterData[n*4+0]=((characterByte>>0)&0x01)|((characterByte>>3)&0x02);
            characterData[n*4+1]=((characterByte>>1)&0x01)|((characterByte>>4)&0x02);
            characterData[n*4+2]=((characterByte>>2)&0x01)|((characterByte>>5)&0x02);
            characterData[n*4+3]=((characterByte>>3)&0x01)|((characterByte>>6)&0x02);
        }
        
        // get the 4 colour palette for this tile
        PaletteEntry palette = getTilePalette(paletteIndex);

        /*
            5 1
            6 2
            7 3
            4 0
        */
        
        // assemble sprite out of little strips of 8x4 pixels

        drawSpriteSlice(palette, character, characterData, 0, 8, 12);
        drawSpriteSlice(palette, character, characterData, 1, 8, 0);
        drawSpriteSlice(palette, character, characterData, 2, 8, 4);
        drawSpriteSlice(palette, character, characterData, 3, 8, 8);

        drawSpriteSlice(palette, character, characterData, 4, 0, 12);
        drawSpriteSlice(palette, character, characterData, 5, 0, 0);
        drawSpriteSlice(palette, character, characterData, 6, 0, 4);
        drawSpriteSlice(palette, character, characterData, 7, 0, 8);
        
        return character;
    }
    
    private void drawSpriteSlice(PaletteEntry palette, BufferedImage image, int[] characterData, int offset, int x, int y)
    {
        offset*=32;
        
        // block of 8 columns
        for(int n=7;n>=0;n--)
        {
            // each column is 4 pixels tall
            for(int m=3;m>=0;m--)
            {
                image.setRGB(x+n,y+m, palette.getColour(characterData[offset]).getRGB());
                offset++;
            }
        }
    }
    
    
}
