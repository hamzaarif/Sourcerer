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
package edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema;

import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterVersionToFqnVersionTable extends DatabaseTable {
  /*
   *          cluster_version_to_fqn_version table
   * +--------------------+-----------------+-------+--------+
   * | Column name        | Type            | Null? | Index? |
   * +--------------------+-----------------+-------+--------+
   * | cluster_version_id | BIGINT UNSIGNED | No    | Yes    |
   * | fqn_version_id     | BIGINT UNSIGNED | No    | Yes    |
   * +--------------------+-----------------+-------+--------+   
   */
  public static final ClusterVersionToFqnVersionTable TABLE = new ClusterVersionToFqnVersionTable();
  
  public static final Column<Integer> CLUSTER_VERSION_ID = TABLE.addIDColumn("cluster_version_id", false).addIndex();
  public static final Column<Integer> FQN_VERSION_ID = TABLE.addIDColumn("fqn_version_id", false).addIndex();
    
  private ClusterVersionToFqnVersionTable() {
    super("cluster_version_to_fqn_version");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer clusterVersionID, Integer fqnVersionID) {
    return TABLE.makeInsert(CLUSTER_VERSION_ID.to(clusterVersionID), FQN_VERSION_ID.to(fqnVersionID));
  }
}