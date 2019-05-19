package project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Memory {   
    final static int [] memory = new int[2000]; // int array to implement memory
    
    public static void main(String args[]) {
        try {
            //Create a File object and a scanner to read it
            @SuppressWarnings("resource")
			Scanner CPU_reader = new Scanner(System.in);
            File file = null;
            if(CPU_reader.hasNextLine())    // read file name from CPU
            {
                file = new File(CPU_reader.nextLine());
                
                if(!file.exists()) //exit if file not found
                {
                    System.out.println("File not found");
                    System.exit(0);
                }
            }
            
            // call function to read file and initialize memory array
            readFile(file);

            String line;
            int temp2;
            /*
                This while loop will keep on reading instructions from the CPU process
                and perform the read or write function appropriately
            */
            while(true)
            {
                if(CPU_reader.hasNext())
                {
                    line = CPU_reader.nextLine(); //read the comma delimited line sent by the CPU
                    if(!line.isEmpty())
                    {
                        String [] j = line.split(","); //split the line to get the necessary tokens
                        
                        //  if first token is 1 then CPU is requesting to read 
                        //  from an address
                        if(j[0].equals("1"))    
                        {
                            temp2 = Integer.parseInt(j[1]);
                            System.out.println(memory[temp2]);// send requested data to CPU 
                        }
                        
                        //  else it will be 2, which means CPu is requesting to 
                        //  write data at a particular address
                        else
                        {
                            int i1 = Integer.parseInt(j[1]);
                            int i2 = Integer.parseInt(j[2]);
                            memory[i1] = i2;
                            // invoke the write function
                        }
                    }
                    else 
                        break;
                }
                else
                    break;
            }
            
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    // function to read the data in the address provided by the CPU
    
    // function to write data into an address based on instruction given by CPU

    // function to read instructions from file and initialize memory
    private static void readFile(File file) {
        try 
        {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            String temp;
            int temp_int;
            int i = 0;

            /*
            *   Read the file to load memory instructions
            */
            while(scanner.hasNext())
            {
                //if integer found then write to memory array
                if(scanner.hasNextInt())
                {
                    temp_int = scanner.nextInt();
                    memory[i++] = temp_int;
                }
                
                else
                {
                    temp = scanner.next();
                    // if token starts with ".", then move the counter appropriately
                    if(temp.charAt(0) == '.')
                        i = Integer.parseInt(temp.substring(1));
                    
                    // else if the token is a comment then go to next line
                    else if(temp.equals("//"))
                        scanner.nextLine();
                    
                    // skip the line if anything else
                    else
                        scanner.nextLine();
                }
            }
        } 
        catch (FileNotFoundException ex) 
        {
            ex.printStackTrace();
        }
    }
}