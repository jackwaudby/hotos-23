#!/bin/bash

cd ${NEO4J_HOME}

./bin/neo4j-admin database import full \
    --id-type=INTEGER \
    --ignore-empty-strings=true \
    --bad-tolerance=0 \
    --nodes=Place="${NEO4J_HEADER_DIR}/static/Place${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/Place -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Organisation="${NEO4J_HEADER_DIR}/static/Organisation${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}initial_snapshot/static/Organisation -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=TagClass="${NEO4J_HEADER_DIR}/static/TagClass${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/TagClass -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Tag="${NEO4J_HEADER_DIR}/static/Tag${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/Tag -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Forum="${NEO4J_HEADER_DIR}/dynamic/Forum${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Forum -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Person="${NEO4J_HEADER_DIR}/dynamic/Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Message:Comment="${NEO4J_HEADER_DIR}/dynamic/Comment${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --nodes=Message:Post="${NEO4J_HEADER_DIR}/dynamic/Post${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Post -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_PART_OF="${NEO4J_HEADER_DIR}/static/Place_isPartOf_Place${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/Place_isPartOf_Place -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_SUBCLASS_OF="${NEO4J_HEADER_DIR}/static/TagClass_isSubclassOf_TagClass${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/TagClass_isSubclassOf_TagClass -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_LOCATED_IN="${NEO4J_HEADER_DIR}/static/Organisation_isLocatedIn_Place${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/Organisation_isLocatedIn_Place -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_TYPE="${NEO4J_HEADER_DIR}/static/Tag_hasType_TagClass${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/static/Tag_hasType_TagClass -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_CREATOR="${NEO4J_HEADER_DIR}/dynamic/Comment_hasCreator_Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment_hasCreator_Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_LOCATED_IN="${NEO4J_HEADER_DIR}/dynamic/Comment_isLocatedIn_Country${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment_isLocatedIn_Country -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=REPLY_OF="${NEO4J_HEADER_DIR}/dynamic/Comment_replyOf_Comment${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment_replyOf_Comment -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=REPLY_OF="${NEO4J_HEADER_DIR}/dynamic/Comment_replyOf_Post${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment_replyOf_Post -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=CONTAINER_OF="${NEO4J_HEADER_DIR}/dynamic/Forum_containerOf_Post${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Forum_containerOf_Post -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_MEMBER="${NEO4J_HEADER_DIR}/dynamic/Forum_hasMember_Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Forum_hasMember_Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_MODERATOR="${NEO4J_HEADER_DIR}/dynamic/Forum_hasModerator_Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Forum_hasModerator_Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_TAG="${NEO4J_HEADER_DIR}/dynamic/Forum_hasTag_Tag${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Forum_hasTag_Tag -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_INTEREST="${NEO4J_HEADER_DIR}/dynamic/Person_hasInterest_Tag${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_hasInterest_Tag -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_LOCATED_IN="${NEO4J_HEADER_DIR}/dynamic/Person_isLocatedIn_City${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_isLocatedIn_City -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=KNOWS="${NEO4J_HEADER_DIR}/dynamic/Person_knows_Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_knows_Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=LIKES="${NEO4J_HEADER_DIR}/dynamic/Person_likes_Comment${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_likes_Comment -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=LIKES="${NEO4J_HEADER_DIR}/dynamic/Person_likes_Post${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_likes_Post -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_CREATOR="${NEO4J_HEADER_DIR}/dynamic/Post_hasCreator_Person${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Post_hasCreator_Person -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_TAG="${NEO4J_HEADER_DIR}/dynamic/Comment_hasTag_Tag${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Comment_hasTag_Tag -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=HAS_TAG="${NEO4J_HEADER_DIR}/dynamic/Post_hasTag_Tag${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Post_hasTag_Tag -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=IS_LOCATED_IN="${NEO4J_HEADER_DIR}/dynamic/Post_isLocatedIn_Country${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Post_isLocatedIn_Country -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=STUDY_AT="${NEO4J_HEADER_DIR}/dynamic/Person_studyAt_University${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_studyAt_University -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --relationships=WORK_AT="${NEO4J_HEADER_DIR}/dynamic/Person_workAt_Company${NEO4J_HEADER_EXTENSION}$(${FIND_COMMAND} ${NEO4J_CSV_DIR}/initial_snapshot/dynamic/Person_workAt_Company -type f -name ${NEO4J_PART_FIND_PATTERN} -printf ',%p')" \
    --delimiter '|'

cd ${MAMMOTH_HOME}/scripts

# ./tools/run.py --cores 2 -- --mode bi --format csv --scale-factor ${SF}  --output-dir out-sf${SF}/ --explode-edges --epoch-millis  --format-options header=false,quoteAll=true,compression=gzip


   
