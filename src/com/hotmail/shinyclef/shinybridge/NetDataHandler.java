package com.hotmail.shinyclef.shinybridge;

/**
 * User: Shinyclef
 * Date: 12/07/13
 * Time: 11:05 PM
 */

public class NetDataHandler
{
    public NetDataHandler()
    {

    }

    public String processInput(String input)
    {
        ShinyBridge.log(input);
        return "Echo: " + input;
    }
}
