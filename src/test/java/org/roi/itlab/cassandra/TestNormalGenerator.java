package org.roi.itlab.cassandra; /**
 * Created by neron on 12.03.2017.
 */

//для 5000 тестов по 10000 сгенерированных элементов в каждом, всегда строится нормально
import java.util.*;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.special.Erf;
import org.junit.Assert;
import org.junit.Test;
import org.roi.itlab.cassandra.random_attributes.NormalGenerator;


public class TestNormalGenerator {
        static int num = 10000;

        @Test
        public void testDistribution() {
            //int result=0; // для проверки
            //for (int t = 0; t < 5000; t++) {
                //задаем дискретные значения x-стаж, у-средний Skill, z-среднеквадратическое отклонение, g-стаж для которого ищем Skill
                double[] x = new double[]{0, 1, 5};
                double[] y = new double[]{0, 1, 25};
                double[] z = new double[]{0, 0.1, 2.5};
                double g = 4;
                int kor=5; //для корекции выборки, выкидываем по 5 крайних элементов, т.к , возможно , это промахи
                // double ka=0.120720674867;// процентная точка распределения Колмогорова для 100 элемнтов
                //double ka=Math.sqrt((-1/(2*num))*Math.log(0.05));//-(1/(6*num)); // не правльно считает
                double ka = 0.122220674867;// для 10000
                double std = 0;
                double mean = 0;
                int num = 10000;
                double max = 0;
                double[] value = new double[num];// храним сгенерированнвые Skill для определенного стажа
                double[] valuey = new double[num];//храним координаты y для функции распределения(практической)

                NormalGenerator normalGenerator = new NormalGenerator(new MersenneTwister(1), x, y, z);
            //запись Skill в массив
                for (int i = 0; i < num; i++) {
                    value[i] = normalGenerator.getRandomDouble(g);
                    mean += value[i];
                }
                mean = mean / num;
                Arrays.sort(value);
                for (int i = 0; i < num; i++) {
                    std += (value[i] - mean) * (value[i] - mean);
                }
                std = Math.sqrt(std / num);


                for (int i = kor; i < num - kor; i++) {
                    valuey[i] = FuncPrakt(value[i], value);// знчение функции рапределения(практической) в точке x[i]
                    //nw(value[i],mean,std)-знчение функции рапределения(теоритической) в точке x[i]
                    if (Math.abs(nw(value[i], mean, std) - valuey[i]) > max) {   // поиск максимума между теоритической и практической фукцией распределения
                        max = Math.abs(nw(value[i], mean, std) - valuey[i]);
                    }
                }

                Assert.assertTrue(max<=ka);
        //}

        }
        // функция распределения построенная по точкам(практическая)
        public static double FuncPrakt(double s,double[] p) {
            double r=0;
            for (int j=0; j<num;j++ ){
                if(p[j]<=s){
                    r++;
                }
            }
            r=r/num;
            return r;
        }

        //функция нормального распределения(теоретическая, с которой будем сравнивать)
        public static double nw (double x, double med,double sko){
            double l;
            Erf o = null;
            double t=o.erf((x-med)/(1.41421356*sko));
            l=0.5*(1+t);

            return l;
        }

    }
