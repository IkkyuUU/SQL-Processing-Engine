# SQL-Processing-Engine
A application to compile MF/EMF SQL queries

# How to run the SQL engine
First of all, make sure that you have downloaded JDBC for PostgresSQL.

Then, Download all the files into your IDE. Run the "SimpleInterface.java" and you will get a text box for you to input your MF/EMF queries.

Note: This SQL engine can only take MF/EMF queries. If you want to know more about what are MF/EMF queries, please refer to two papers in the repository. I have included the sample inputs in the file named "input.txt".

# About the input
In order to reduce the workload of processing the inputs strings, we have defined what the inputs should look like. The rules are as follow:

(1) The first line contains the SELECT ATTRIBUTES(S)
(2) The second line contains the number of GROUPING VARIABLES(n)
(3) The third line contains the GROUPING ATTRIBUTES(V)
(4) The fourth line contains the F-VECTOR([F])
(5) The fifth line contains the SELECT CONDITION-VECTOR([σ])
(6) The sixth line contains the HAVING_CONDITION(G)

For example, an EMF query

  select cust, sum(x.quant), sum(y.quant), sum(z.quant) from sales

        group by cust: x, y, z
        
        such that x.state = ‘NY’
        
              and y.state = ‘NJ’
              
              and z.state = ‘CT’
              
  having sum(x.quant) > 2 * sum(y.quant) or avg(x.quant) > avg(z.quant);

should be inputed as follow:
cust, sum_1_quant, sum_2_quant, sum_3_quant

3

cust

sum_1_quant, avg_1_quant, sum_2_quant, sum_3_quant, avg_3_quant

state_1=‘NY', state_2=‘NJ', state_3=‘CT'

sum_1_quant > 2 * sum_2_quant or avg_1_quant > avg_3_quant
