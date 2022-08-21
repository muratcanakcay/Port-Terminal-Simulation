package classes;

import java.time.Instant;

public class Utils
{
    public static class Clock
    {
        private static long startTime;
        private static int  simulationSpeed;

        public Clock(int simulationSpeed_)
        {
            startTime = Instant.now().getEpochSecond();
            simulationSpeed = simulationSpeed_;
        }

        public static void GetSimulationTime()
        {
            System.out.println("Start: " + startTime);
            long ut = System.currentTimeMillis() / 1000L;
            System.out.println("Now: " + ut);
            System.out.println("Passed: " + ((ut-startTime) * simulationSpeed));
        }

        public static int GetSimulationSpeed()
        {
            return simulationSpeed;
        }
    }

    public int countOccurences(String someString, char searchedChar)
    {
        int count = 0;

        for (int i = 0; i < someString.length(); i++)
        {
            if (someString.charAt(i) == searchedChar)
            {
                count++;
            }
        }
        return count;
    }

    public String getIndexOfLargest(long[][] array)
    {
        if (array == null || array.length == 0) return "-1"; // null or empty

        int largestR = 0;
        int largestC = 0;
        for (int i = 0; i < array.length; i++)
        {
            for (int j = 0; j < array.length; j++)
            {
                if (array[i][j] > array[largestR][largestC])
                {
                    largestR = i;
                    largestC = j;
                }
            }
        }
        return largestR + "," + largestC; // position of the first largest found
    }
}
