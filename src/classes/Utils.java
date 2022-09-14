package classes;

public class Utils
{
    public static class Clock
    {
        private static int currentTime = 0;
        private static int simulationSpeed;
        public Clock(int simulationSpeed_) { simulationSpeed = simulationSpeed_; }
        public static int GetSimulationTime()
        {
            return currentTime;
        }
        public static int GetSimulationSpeed()
        {
            return simulationSpeed;
        }
        public static void  tick() { ++currentTime; }
    }
}
