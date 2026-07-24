package top.aurora.lordofmysteries.commission;

record DynamicCaseManifestationPlan(
        Offset subject, Offset routine, Offset affected, Offset evidence) {

    static DynamicCaseManifestationPlan forProfile(DynamicCaseProfile profile) {
        int rotation = Math.floorMod(profile.instanceId().hashCode(), 4);
        return new DynamicCaseManifestationPlan(
                rotate(new Offset(2, 1), rotation),
                rotate(new Offset(0, 2), rotation),
                rotate(new Offset(-2, 1), rotation),
                rotate(new Offset(0, -2), rotation));
    }

    private static Offset rotate(Offset offset, int quarterTurns) {
        int x = offset.x();
        int z = offset.z();
        for (int turn = 0; turn < quarterTurns; turn++) {
            int nextX = -z;
            z = x;
            x = nextX;
        }
        return new Offset(x, z);
    }

    record Offset(int x, int z) {
        Offset {
            if (Math.abs(x) > 3 || Math.abs(z) > 3) {
                throw new IllegalArgumentException(
                        "dynamic case manifestation offset is out of range");
            }
        }
    }
}
