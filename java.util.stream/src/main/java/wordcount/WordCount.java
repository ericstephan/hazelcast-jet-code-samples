/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wordcount;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.stream.DistributedCollectors;
import com.hazelcast.jet.stream.IStreamMap;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WordCount {

    static final Pattern PATTERN = Pattern.compile("\\W+");

    public static void main(String[] args) {
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        IMap<Integer, String> source = instance1.getMap("source");

        source.put(0, "It was the best of times, "
                + "it was the worst of times, "
                + "it was the age of wisdom, "
                + "it was the age of foolishness, "
                + "it was the epoch of belief, "
                + "it was the epoch of incredulity, "
                + "it was the season of Light, "
                + "it was the season of Darkness, "
                + "it was the spring of hope, "
                + "it was the winter of despair, "
                + "we had everything before us, "
                + "we had nothing before us, "
                + "we were all going direct to Heaven, "
                + "we were all going direct the other way-- "
                + "in short, the period was so far like the present period, that some of "
                + "its noisiest authorities insisted on its being received, for good or for "
                + "evil, in the superlative degree of comparison only.");

        source.put(1, "There were a king with a large jaw and a queen with a plain face, on the "
                + "throne of England; there were a king with a large jaw and a queen with "
                + "a fair face, on the throne of France. In both countries it was clearer "
                + "than crystal to the lords of the State preserves of loaves and fishes, "
                + "that things in general were settled for ever.");

        source.put(2, "It was the year of Our Lord one thousand seven hundred and seventy-five. "
                + "Spiritual revelations were conceded to England at that favoured period, "
                + "as at this. Mrs. Southcott had recently attained her five-and-twentieth "
                + "blessed birthday, of whom a prophetic private in the Life Guards had "
                + "heralded the sublime appearance by announcing that arrangements were "
                + "made for the swallowing up of London and Westminster. Even the Cock-lane "
                + "ghost had been laid only a round dozen of years, after rapping out its "
                + "messages, as the spirits of this very year last past (supernaturally "
                + "deficient in originality) rapped out theirs. Mere messages in the "
                + "earthly order of events had lately come to the English Crown and People, "
                + "from a congress of British subjects in America: which, strange "
                + "to relate, have proved more important to the human race than any "
                + "communications yet received through any of the chickens of the Cock-lane "
                + "brood.");

        IStreamMap<Integer, String> streamMap = IStreamMap.streamMap(source);

        IMap<String, Integer> counts = streamMap.stream()
                .flatMap(m -> Stream.of(PATTERN.split(m.getValue().toLowerCase())))
                .collect(DistributedCollectors.toIMap(m -> m,
                        m -> 1,
                        (left, right) -> left + right));

        System.out.println("Counts=" + counts.entrySet());

        Hazelcast.shutdownAll();
    }
}
