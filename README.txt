=================================
 Environment
=================================

Prerequisites:
- java 1.7+
- any linux OS

Additional tools:
- Intellij IDEA 14+
- Gradle 2+


=================================
 Algorithms/References
=================================

- cs2b  : CoSolver-based with 2-OPT and Bit-flip [2]
- cs2sa : CoSolver-based with 2-OPT and Simulated Annealing [1]
- ma2b  : Memetic Algorithm with 2-OPT and Bit-flip [1]

[1] El Yafrani, Mohamed, and Belaïd Ahiod. "Population-based vs. Single-solution Heuristics for the Travelling Thief Problem." Proceedings of the 2016 on Genetic and Evolutionary Computation Conference. ACM, 2016.

[2] Yafrani, Mohamed El, and Belaïd Ahiod. "Cosolver2B: An Efficient Local Search Heuristic for the Travelling Thief Problem." arXiv preprint arXiv:1603.07051 (2016).

=================================
 Important notes
=================================

1. make sure all the files in bins directory are executable:
   chmod +x bins -R

2. due to space constraint, TTP data are removed from the database directory,
   except for a280-based instances. TTP instances are available at:
   http://cs.adelaide.edu.au/~optlog/CEC2014COMP_InstancesNew/

3. the linkern binary file in the bins/linkern/ folder is a modified one.
   The code source have been slightly hacked in order to improve the randomness of
   the generated initial tours

4. database paths can be modified in the "config.properties"
