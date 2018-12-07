# TTPlab

This project is a java implementation of heuristic algorithms to solve
the [Travelling Thief Problem](http://cs.adelaide.edu.au/~optlog/research/combinatorial.php) [3].

If you are more interested in comparing your algorithm with ours, 
the jar executable version with python scripts is also available at 
[this link](https://github.com/yafrani/ttplab-jar).


# Environment

## Prerequisites
- java 1.7+
- any linux OS

## Additional tools
- Intellij IDEA 14+
- Gradle 2+


# Algorithms

- cs2b  : CoSolver-based with 2-OPT and Bit-flip [2]
- cs2sa : CoSolver-based with 2-OPT and Simulated Annealing [1]
- ma2b  : Memetic Algorithm with 2-OPT and Bit-flip [1]


# Important notes

1. make sure all the files in bins directory are executable:
   chmod +x bins -R

2. due to space constraint, TTP data are removed from the database directory,
   except for a280-based instances. TTP instances are available at:
   http://cs.adelaide.edu.au/~optlog/CEC2014COMP_InstancesNew/

3. the linkern binary file in the bins/linkern/ folder is a modified one.
   The source codes have been slightly hacked in order to improve the randomness of
   the generated initial tours

4. database paths can be modified in the "config.properties"

5. there was a metric issue in the TTP objective function in the older version of the code (before Nov 14, 2016), 
   please download the current version for comparison purposes


# Contact information

For additional information, please send an email to *m dot elyafrani at gmail dot com*.


# References

[1] El Yafrani, Mohamed, and Belaïd Ahiod. "Population-based vs. Single-solution Heuristics for the Travelling Thief Problem." Genetic and Evolutionary Computation Conference (GECCO), ACM, 2016.

[2] El Yafrani, Mohamed, and Belaïd Ahiod. "Cosolver2B: An Efficient Local Search Heuristic for the Travelling Thief Problem." IEEE/ACS International Conference of Computer Systems and Applications (AICCSA), IEEE, 2015.

[3] Bonyadi, Mohammad Reza, Zbigniew Michalewicz, and Luigi Barone. "The travelling thief problem: the first step in the transition from theoretical problems to realistic problems." 2013 IEEE Congress on Evolutionary Computation. IEEE, 2013.
