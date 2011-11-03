/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.tools.java.utilization.identifier;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.sourcerer.tools.java.utilization.entropy.JarEntopyCalculator;
import edu.uci.ics.sourcerer.tools.java.utilization.entropy.JarEntropyCalculatorFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.JarCollection;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;



/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Identifier {
  private Identifier() {
  }
  
  public static LibraryCollection identifyLibraries(TaskProgressLogger task, JarCollection jars) {
    task.start("Identifying libraries in " + jars.size() + " jar files");
    
    task.start("Computing jar entropies", "jars processed", 500);
    JarEntopyCalculator calc = JarEntropyCalculatorFactory.makeCalculator();
    final Map<Jar, Double> entropies = new HashMap<>();
    for (Jar jar : jars) {
      entropies.put(jar, calc.compute(jar));
      task.progress();
    }
    task.finish();
    
    LibraryCollection libraries = new LibraryCollection();
    Set<Jar> processed = new HashSet<>();
    
//    task.start("Dumping entropy information");
//    TreeSet<Jar> jarEntropies = new TreeSet<>(new Comparator<Jar>() {
//      @Override
//      public int compare(Jar o1, Jar o2) {
//        int cmp = Double.compare(entropies.get(o1), entropies.get(o2));
//        if (cmp == 0) {
//          return Integer.compare(o1.hashCode(), o2.hashCode());
//        } else {
//          return cmp;
//        }
//      }});
//    for (Jar jar : jars) {
//      jarEntropies.add(jar);
//    }
//    while (!jarEntropies.isEmpty()) {
//      Jar smallest = jarEntropies.pollFirst();
//      if (entropies.get(smallest) > 0) {
//        task.report(smallest + ": " + entropies.get(smallest));
//        for (FqnFragment fqn : smallest.getFqns()) {
//          task.report("  " + fqn.getFqn());
//        }
//      }
//    }
//    task.finish();
    
    task.start("Identifying jar clusters");
    for (Jar jar : jars) {
      // Has this jar been processed?
      if (!processed.contains(jar)) {
        processed.add(jar);
        // Find the transitive closure of related FQNs
        TreeSet<Jar> relatedJars = new TreeSet<>(new Comparator<Jar>() {
          @Override
          public int compare(Jar o1, Jar o2) {
            int cmp = Double.compare(entropies.get(o1), entropies.get(o2));
            if (cmp == 0) {
              return Integer.compare(o1.hashCode(), o2.hashCode());
            } else {
              return cmp;
            }
          }});
        Set<FqnFragment> relatedFqns = new HashSet<>();
        
        Deque<Jar> stack = new LinkedList<>();
        stack.push(jar);
        while (!stack.isEmpty()) {
          Jar next = stack.pop();
          relatedJars.add(next);
          for (FqnFragment fqn : next.getFqns()) {
            relatedFqns.add(fqn);
            for (Jar j : fqn.getJars()) {
              if (!processed.contains(j)) {
                stack.push(j);
                processed.add(j);
              }
            }
          }
        }
        
        Map<FqnFragment, Library> fqnMapping = new HashMap<>();
        
        // Take the jar with the smallest entropy
        // If its FQNs are unique, make it the seed of a library
        while (!relatedJars.isEmpty()) {
          Jar smallest = relatedJars.pollFirst();
          // Are there no libraries yet?
          if (fqnMapping.isEmpty()) {
            Library library = new Library();
            library.addJar(smallest);
            for (FqnFragment fqn : smallest.getFqns()) {
              library.addFqn(fqn);
              fqnMapping.put(fqn, library);
            }
            libraries.addLibrary(library);
          } else {
            // Find candidate libraries
            Set<Library> candidates = new HashSet<>();
            for (FqnFragment fqn : smallest.getFqns()) {
              Library library = fqnMapping.get(fqn);
              if (library != null) {
                candidates.add(library);
              }
            }
            
            // Is there no candidate library?
            if (candidates.isEmpty()) {
              Library library = new Library();
              library.addJar(smallest);
              for (FqnFragment fqn : smallest.getFqns()) {
                library.addFqn(fqn);
                fqnMapping.put(fqn, library);
              }
              libraries.addLibrary(library);
            } 
            // Is there only one candidate library? 
            else if (candidates.size() == 1) {
              Library library = candidates.iterator().next();
              library.addJar(smallest);
              for (FqnFragment fqn : smallest.getFqns()) {
                library.addFqn(fqn);
                fqnMapping.put(fqn, library);
              }
            }
            // There are multiple candidate libraries
            else {
              for (Library candidate : candidates) {
                candidate.addJar(smallest);
              }
            }
          }
        }
      }
    }
    task.finish();
    
    task.finish();
    
    return libraries;
  }
}