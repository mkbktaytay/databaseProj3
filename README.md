# database_proj3
Comparing Opengauss and PostgreSQL
该实验使用java程序连接到sql，每一种对比测试使用一个java文件，依次执行opengauss和pgsql。java文件运行后会自动打印测试结果，如运行时间和成功与否。
下面是对每个java代码的解释：
Access.java:
该程序用于测试 PostgreSQL 和 openGauss 数据库的访问控制，包括基于角色的访问控制（RBAC）和细粒度访问控制（行或列级权限）。
Authentication.java:
该程序测试 PostgreSQL 和 openGauss 数据库的三种认证方式：密码认证、Kerberos认证、以及SSL/TLS认证，验证数据库连接的安全性与认证机制。
Concurrent.java:
该程序测试 PostgreSQL 和 openGauss 数据库在多用户并发访问下的性能，通过模拟不同数量的用户并发执行查询，计算每秒事务数（TPS）和总执行时间，评估数据库性能.
DataVolume.java:
这个程序测试 PostgreSQL 和 openGauss 数据库在大数据量下的性能，通过逐步增加数据库中的数据行数，评估两种数据库在处理大规模数据和高并发插入操作时的性能表现。
Integrity.java：
该程序通过在 PostgreSQL 和 openGauss 中插入测试数据，模拟数据库故障与恢复过程，验证数据在故障后的完整性与一致性，并记录恢复和验证的时间，确保数据未丢失或损坏。
LatencyTest.java：
该程序通过模拟并发用户对 PostgreSQL 和 openGauss 数据库进行查询操作，测量不同并发用户数（从 10 到 100）下的延迟性能，包括最小响应时间、最大响应时间和平均响应时间，同时记录总请求时间和请求数量，以评估数据库的性能表现。
Recovery.java：
该Java程序模拟了PostgreSQL和openGauss数据库的故障和恢复过程，测试并输出每次恢复的时间。程序通过停止和启动数据库服务，测量数据库从故障发生到恢复所需的时间。
Restore.java：
该Java程序实现了数据库的备份、加密、恢复和数据验证功能，支持PostgreSQL和openGauss数据库。主要流程如下：
生成AES密钥：生成并保存一个128位的AES密钥，用于加密和解密数据库备份文件。
备份和加密数据：执行数据库备份命令（例如pg_dump），然后使用AES密钥加密备份文件。
恢复数据并验证：解密备份文件，恢复数据到数据库（例如使用pg_restore），并通过查询验证数据完整性（如表行数）。
SimpleSelect.java：
该Java程序测试PostgreSQL和openGauss数据库执行不同SQL查询（如简单查询、索引查询、连接查询和聚合查询）的性能。每个查询执行10次，记录执行时间，并监控CPU和内存使用情况。每0.1秒打印一次资源状态，最后输出每个查询的平均执行时间和资源使用情况，帮助评估查询性能和系统负载。
TPS.java：
该Java程序测试PostgreSQL和openGauss数据库的事务性能（TPS）。它执行四种类型的SQL查询（插入、更新、查询、删除）并测量每种查询的TPS（每秒事务数）。使用单个线程执行事务，并通过ExecutorService管理并发任务。每个查询执行5次事务，记录执行时间并计算TPS。通过设置事务隔离级别和自动重试机制，程序能够处理死锁等错误。
key.java：
该Java程序生成一个128位的AES加密密钥，并将其保存到指定的文件路径中。使用KeyGenerator生成密钥，通过FileOutputStream将密钥的字节编码写入文件。成功生成并保存密钥后，程序输出“密钥文件已生成！”信息。
