// ----------------------------------------------------------------------------
// Copyright 2006-2008, Martin D. Flynn
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Thread pool manager
// ----------------------------------------------------------------------------
// Change History:
//  2006/03/26  Martin D. Flynn
//     -Initial release
//  2006/04/03  Martin D. Flynn
//     -Removed reference to JavaMail api imports
//  2006/06/30  Martin D. Flynn
//     -Repackaged to "org.opengts.util"
//  2006/11/28  Martin D. Flynn
//     -Added method "setMaxSize(size)"
// ----------------------------------------------------------------------------
package com.dignity.alarmdeamon.dateutil;

import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadPool
{
    
    // ------------------------------------------------------------------------

    private static final int MAX_POOL_SIZE  = 20;
    
    public  static final int STOP_WAITING   = -1;
    public  static final int STOP_NEVER     = 0;
    public  static final int STOP_NOW       = 1;
    private static Logger logger = LogManager.getLogger(ThreadPool.class.getName());

    // ------------------------------------------------------------------------

    private ThreadGroup     poolGroup       = null;
    private java.util.List  jobThreadPool   = null;
    private int             maxPoolSize     = MAX_POOL_SIZE;
    private int             threadId        = 1;
    private java.util.List  jobQueue        = null;
    private int             waitingCount    = 0;
    private int             stopThreads     = STOP_NEVER;
    
    public ThreadPool(String name)
    {
        this(name, MAX_POOL_SIZE);
    }
    
    public ThreadPool(String name, int maxPoolSize)
    {
        super();
        this.poolGroup     = new ThreadGroup((name != null)? name : "ThreadPool");
        this.jobThreadPool = new Vector();
        this.jobQueue      = new Vector();
        this.setMaxSize(maxPoolSize);
    }
    
    // ------------------------------------------------------------------------
    
    public String getName()
    {
        return this.getThreadGroup().getName();
    }
    
    public String toString()
    {
        return this.getName();
    }
    
    public boolean equals(Object other)
    {
        return (this == other); // equals only if same object
    }
    
    // ------------------------------------------------------------------------
    
    public ThreadGroup getThreadGroup()
    {
        return this.poolGroup;
    }
    
    // ------------------------------------------------------------------------

    public int getSize()
    {
        int size = 0;
        synchronized (this.jobThreadPool) {
            size = this.jobThreadPool.size();
        }
        return size;
    }
    
    public void setMaxSize(int maxSize)
    {
        this.maxPoolSize = (maxSize > 0)? maxSize : MAX_POOL_SIZE;
    }
    
    public int getMaxSize()
    {
        return this.maxPoolSize;
    }
    
    // ------------------------------------------------------------------------
    
    public void run(Runnable job)
    {
        synchronized (this.jobThreadPool) { // <-- modification of threadPool is likely
            synchronized (this.jobQueue) { // <-- modification of job queue mandatory
                // It's possible that we may end up adding more threads than we need if this
                // section executes multiple times before the newly added thread has a chance 
                // to pull a job off the queue.
                this.jobQueue.add(job);
                if ((this.waitingCount == 0) && (this.jobThreadPool.size() < this.maxPoolSize)) {
                    ThreadJob tj = new ThreadJob(this, (this.getName() + "_" + (this.threadId++)));
                    this.jobThreadPool.add(tj);
                    logger.debug("New Thread: " + tj.getName() + " [" + this.getMaxSize() + "]");
                }
                this.jobQueue.notify(); // notify a waiting thread
            }
        }
    }
    
    // ------------------------------------------------------------------------
    
    public void stopThreads()
    {
        synchronized (this.jobQueue) {
            this.stopThreads = STOP_WAITING;
            this.jobQueue.notifyAll();
        }
    }
    
    protected void _removeThread(ThreadJob thread)
    {
        if (thread != null) {
            synchronized (this.jobThreadPool) {
                this.jobThreadPool.remove(thread);
            }
        }
    }
    
    // ------------------------------------------------------------------------

    private static class ThreadJob
        extends Thread
    {
        private Runnable job = null;
        private ThreadPool threadPool = null;
        
        public ThreadJob(ThreadPool pool, String name) {
            super(pool.getThreadGroup(), name);
            this.threadPool = pool;
            this.start(); // auto start
        }
       
        public void run() {
    
            /* loop forever */
            while (true) {
    
                /* get next job */
                // 'this.job' is always null here
                boolean stop = false;
                synchronized (this.threadPool.jobQueue) {                    
                    while (this.job == null) {
                        if (this.threadPool.stopThreads == STOP_NOW) {
                            stop = true;
                            break;
                        } else
                        if (this.threadPool.jobQueue.size() > 0) {
                            this.job = (Runnable)this.threadPool.jobQueue.remove(0);
                        } else
                        if (this.threadPool.stopThreads == STOP_WAITING) {
                            stop = true;
                            break;
                        } else {
                            this.threadPool.waitingCount++;
                            try { this.threadPool.jobQueue.wait(20000); } catch (InterruptedException ie) {}
                            this.threadPool.waitingCount--;
                        }
                    }
                }
                if (stop) { break; }                              
                this.job.run();
                this.job = null;
                
            }
            
            /* remove thread from pool */
            this.threadPool._removeThread(this);         
        }        
    }    
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        ThreadPool pool = new ThreadPool("Test", 3);
        for (int i = 0; i < 12; i++) {
            final int n = i;
           logger.debug("Job " + i);
            Runnable job = new Runnable() {
                int num = n;
                public void run() {
                    logger.trace("Starting Job: " + this.getName());
                    try { Thread.sleep(2000 + (num * 479)); } catch (Throwable t) {}
                    logger.trace("Stopping Job:                " + this.getName());
                }
                public String getName() {
                    return "[" + Thread.currentThread().getName() + "] " + num;
                }
            };
            pool.run(job);
            try { Thread.sleep(500 + (i * 58)); } catch (Throwable t) {}
        }
        logger.trace("Stop Threads");
        pool.stopThreads();
    }
    
}