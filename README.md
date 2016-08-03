# Environment

## Prerequisites
- java 1.7+
- any linux OS

## Additional tools
- Intellij IDEA 14+
- Gradle 2+


# Algorithms/References

- cs2b  : CoSolver-based with 2-OPT and Bit-flip [2]
- cs2sa : CoSolver-based with 2-OPT and Simulated Annealing [1]
- ma2b  : Memetic Algorithm with 2-OPT and Bit-flip [1]

[1] "Population-based vs. Single-solution heuristics for the Travelling Thief Problem", GECCO '2016
[2] "Cosolver2B: An Efficient Local Search Heuristic for the Travelling Thief Problem", AICCSA '2015


# Important notes

1. make sure all the files in bins directory are executable:
   chmod +x bins -R

2. due to space constraint, TTP data are removed from the database directory,
   except for a280-based instances. TTP instances are available at:
   http://cs.adelaide.edu.au/~optlog/CEC2014COMP_InstancesNew/

3. database paths can be modified in the "config.properties"
